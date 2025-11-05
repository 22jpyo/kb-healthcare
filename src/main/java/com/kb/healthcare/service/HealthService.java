package com.kb.healthcare.service;

import com.kb.healthcare.domain.HealthEntry;
import com.kb.healthcare.dto.health.HealthDailyResponse;
import com.kb.healthcare.dto.health.HealthMonthlyResponse;
import com.kb.healthcare.dto.health.HealthUploadRequest;
import com.kb.healthcare.repository.HealthEntryRepository;
import com.kb.healthcare.util.HealthNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 헬스 데이터 관리 서비스
 *
 * <p>사용자의 헬스 데이터(걸음 수, 이동 거리, 소모 칼로리)를 업로드하고,
 * 일간/월간 단위로 집계된 통계 데이터를 조회하는 기능을 제공합니다.</p>
 *
 * <p>성능 최적화를 위해 Redis 캐싱을 활용하며, 데이터 업로드 시 관련 캐시를 자동으로 무효화합니다.</p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthEntryRepository entryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HealthNormalizer normalizer;

    /**
     * 일간 데이터 캐시 키 접두사
     */
    private static final String DAILY_KEY = "health:daily::";

    /**
     * 월간 데이터 캐시 키 접두사
     */
    private static final String MONTHLY_KEY = "health:monthly::";

    /**
     * 마지막 업데이트 시간 캐시 키 접두사
     */
    private static final String LASTUPDATE_KEY = "health:lastUpdate::";

    /**
     * 헬스 데이터를 업로드하고 저장합니다.
     *
     * <p>주요 처리 과정:</p>
     * <ol>
     *   <li>clientLastUpdate와 cache의 lastUpdate를 비교</li>
     *   <li>각 헬스 데이터 항목(entry)을 순회하며 처리</li>
     *   <li>시작/종료 시간을 KST(한국 표준시)로 정규화</li>
     *   <li>걸음 수를 정수로 변환</li>
     *   <li>중복 데이터는 업데이트(upsert) 처리</li>
     *   <li>업로드 완료 후 해당 사용자의 일간/월간 캐시 무효화</li>
     * </ol>
     *
     * @param req 헬스 데이터 업로드 요청 (recordKey, 데이터 항목 리스트 포함)
     * @return 성공적으로 저장/업데이트된 데이터 항목 개수
     */
    @Transactional
    public int upload(HealthUploadRequest req) {
        String recordKey = req.recordKey();
        int count = 0;

        // 업데이트 시간을 KST로 정규화
        ZonedDateTime clientLastUpdate = normalizer.toKst(req.lastUpdate());

        // 이전 업로드 시간 조회
        ZonedDateTime lastUploadedAt = null;
        Object raw = redisTemplate.opsForValue().get(LASTUPDATE_KEY + recordKey);
        if (raw instanceof String s) {
            lastUploadedAt = ZonedDateTime.parse(s);
        }

        // 이전보다 같거나 오래된 데이터면 스킵
        if (lastUploadedAt != null && !clientLastUpdate.isAfter(lastUploadedAt)) {
            log.info("[SKIP UPLOAD] 기존 lastUpdate={} >= 요청 lastUpdate={} (recordKey={})",
                    lastUploadedAt, clientLastUpdate, recordKey);
            return 0;
        }

        // 각 헬스 데이터 항목을 순회하며 저장
        for (var entry : req.data().entries()) {
            // 시작/종료 시간을 KST로 정규화
            var start = normalizer.toKst(entry.period().from()).toLocalDateTime();
            var end = normalizer.toKst(entry.period().to()).toLocalDateTime();

            // DB에 upsert (중복 시 업데이트, 없으면 삽입)
            count += entryRepository.upsertEntry(
                    req.recordKey(),
                    start,
                    end,
                    normalizer.toSteps(entry.steps()),
                    entry.distance().value(),
                    entry.calories().value()
            );
        }

        // 데이터 변경으로 인한 캐시 무효화
        redisTemplate.delete(DAILY_KEY + req.recordKey());
        redisTemplate.delete(MONTHLY_KEY + req.recordKey());
        log.info("[CACHE EVICT] recordKey={}", req.recordKey());

        // 데이터 변경으로 인한 lastUpdate 최신화
        redisTemplate.opsForValue().set(LASTUPDATE_KEY + recordKey, clientLastUpdate);
        log.info("[LASTUPDATE SYNC] recordKey={} newLastUpdate={}", recordKey, clientLastUpdate);

        return count;
    }

    /**
     * 특정 사용자의 일간 헬스 데이터 통계를 조회합니다.
     *
     * <p>데이터 처리 과정:</p>
     * <ol>
     *   <li>Redis 캐시에서 먼저 조회 시도 (TTL: 6시간)</li>
     *   <li>캐시 미스 시 DB에서 전체 항목 조회</li>
     *   <li>날짜별로 그룹화하여 걸음 수, 거리, 칼로리 합산</li>
     *   <li>날짜 오름차순으로 정렬</li>
     *   <li>결과를 Redis에 캐싱</li>
     * </ol>
     *
     * @param recordKey 사용자 식별 키
     * @return 일간 헬스 데이터 응답 리스트 (날짜별 집계 데이터)
     * 데이터가 없으면 빈 리스트 반환
     */
    public List<HealthDailyResponse> getDaily(String recordKey) {
        String key = DAILY_KEY + recordKey;

        // 1. 캐시 조회 시도
        @SuppressWarnings("unchecked")
        var cached = (List<HealthDailyResponse>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("[CACHE HIT] key={}", key);
            return cached;
        }

        log.info("[CACHE MISS] key={}", key);

        // 2. DB에서 데이터 조회
        var entries = entryRepository.findByRecordKeyOrderByStartedAtKst(recordKey);
        if (entries.isEmpty()) return List.of();

        // 3. 날짜별로 그룹화
        var grouped = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getStartedAtKst().toLocalDate()));

        // 4. 각 날짜별로 걸음 수, 거리, 칼로리 합산
        var result = grouped.entrySet().stream()
                .map(e -> {
                    var list = e.getValue();
                    // 걸음 수 합산
                    int steps = list.stream().mapToInt(HealthEntry::getSteps).sum();
                    // 이동 거리 합산 (km)
                    BigDecimal distance = list.stream()
                            .map(HealthEntry::getDistanceKm)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 소모 칼로리 합산 (kcal)
                    BigDecimal calories = list.stream()
                            .map(HealthEntry::getCaloriesKcal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new HealthDailyResponse(e.getKey(), steps, calories, distance, recordKey);
                })
                .sorted(Comparator.comparing(HealthDailyResponse::getDaily))
                .toList();

        // 5. Redis에 캐싱 (6시간 TTL)
        redisTemplate.opsForValue().set(key, result, Duration.ofHours(6));
        return result;
    }

    /**
     * 특정 사용자의 월간 헬스 데이터 통계를 조회합니다.
     *
     * <p>데이터 처리 과정:</p>
     * <ol>
     *   <li>Redis 캐시에서 먼저 조회 시도 (TTL: 24시간)</li>
     *   <li>캐시 미스 시 DB에서 전체 항목 조회</li>
     *   <li>연-월(YYYY-MM) 형식으로 그룹화하여 걸음 수, 거리, 칼로리 합산</li>
     *   <li>월 오름차순으로 정렬</li>
     *   <li>결과를 Redis에 캐싱</li>
     * </ol>
     *
     * @param recordKey 사용자 식별 키
     * @return 월간 헬스 데이터 응답 리스트 (월별 집계 데이터)
     * 데이터가 없으면 빈 리스트 반환
     */
    public List<HealthMonthlyResponse> getMonthly(String recordKey) {
        String key = MONTHLY_KEY + recordKey;

        // 1. 캐시 조회 시도
        @SuppressWarnings("unchecked")
        var cached = (List<HealthMonthlyResponse>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("[CACHE HIT] key={}", key);
            return cached;
        }

        log.info("[CACHE MISS] key={}", key);

        // 2. DB에서 데이터 조회
        var entries = entryRepository.findByRecordKeyOrderByStartedAtKst(recordKey);
        if (entries.isEmpty()) return List.of();

        // 3. 연-월(YYYY-MM) 형식으로 그룹화
        var grouped = entries.stream()
                .collect(Collectors.groupingBy(e ->
                        e.getStartedAtKst().getYear() + "-" +
                                String.format("%02d", e.getStartedAtKst().getMonthValue())
                ));

        // 4. 각 월별로 걸음 수, 거리, 칼로리 합산
        var result = grouped.entrySet().stream()
                .map(e -> {
                    var list = e.getValue();
                    // 걸음 수 합산
                    int steps = list.stream().mapToInt(HealthEntry::getSteps).sum();
                    // 이동 거리 합산 (km)
                    BigDecimal distance = list.stream()
                            .map(HealthEntry::getDistanceKm)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 소모 칼로리 합산 (kcal)
                    BigDecimal calories = list.stream()
                            .map(HealthEntry::getCaloriesKcal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new HealthMonthlyResponse(e.getKey(), steps, calories, distance, recordKey);
                })
                .sorted(Comparator.comparing(HealthMonthlyResponse::getMonthly))
                .toList();

        // 5. Redis에 캐싱 (24시간 TTL)
        redisTemplate.opsForValue().set(key, result, Duration.ofHours(24));
        return result;
    }
}