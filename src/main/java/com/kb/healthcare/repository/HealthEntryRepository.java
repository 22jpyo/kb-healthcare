package com.kb.healthcare.repository;

import com.kb.healthcare.domain.HealthEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface HealthEntryRepository extends JpaRepository<HealthEntry, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO health_entry 
            (record_key, started_at_kst, ended_at_kst, steps, distance_km, calories_kcal, created_at, updated_at)
            VALUES (:recordKey, :startedAt, :endedAt, :steps, :distance, :calories, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                steps = VALUES(steps),
                distance_km = VALUES(distance_km),
                calories_kcal = VALUES(calories_kcal),
                updated_at = NOW()
            """, nativeQuery = true)
    int upsertEntry(String recordKey,
                    LocalDateTime startedAt,
                    LocalDateTime endedAt,
                    int steps,
                    BigDecimal distance,
                    BigDecimal calories);

    List<HealthEntry> findByRecordKeyOrderByStartedAtKst(String recordKey);
}
