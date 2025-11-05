package com.kb.healthcare.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * 헬스 데이터 정규화 유틸리티
 * <p>
 * 외부에서 입력된 다양한 형식의 날짜/시간과 숫자 데이터를
 * 시스템 내부 표준 형식으로 변환합니다.
 *
 */
@Component
public class HealthNormalizer {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter FORMAT_1 =
            new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss Z").toFormatter();  // 2024-12-15 12:40:00 +0000
    private static final DateTimeFormatter FORMAT_2 =
            new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter();    // 2024-12-16 20:40:00
    private static final DateTimeFormatter FORMAT_3 =
            new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ssZ").toFormatter(); // 2024-12-15T11:30:00+0000

    public ZonedDateTime toKst(String raw) {
        String s = (raw == null) ? "" : raw.trim();

        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} [+-]\\d{4}")) {
                OffsetDateTime odt = OffsetDateTime.parse(s, FORMAT_1);
                return odt.atZoneSameInstant(KST);
            }

            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                LocalDateTime ldt = LocalDateTime.parse(s, FORMAT_2);
                return ldt.atZone(KST);
            }

            if (s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{4}")) {
                OffsetDateTime odt = OffsetDateTime.parse(s, FORMAT_3);
                return odt.atZoneSameInstant(KST);
            }

            throw new IllegalArgumentException("Invalid date format: " + raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + raw, e);
        }
    }

    public int toSteps(Double raw) {
        if (raw == null) return 0;
        return (int) Math.round(raw);
    }
}