package com.kb.healthcare.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "일간 헬스 데이터 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthDailyResponse implements Serializable {
    @Schema(description = "날짜", example = "2025-11-05")
    private LocalDate daily;

    @Schema(description = "걸음 수", example = "10000")
    private int steps;

    @Schema(description = "소모 칼로리 (kcal)", example = "350.5")
    private BigDecimal calories;

    @Schema(description = "이동 거리 (km)", example = "7.5")
    private BigDecimal distance;

    @Schema(description = "건강 기록 키", example = "3b87c9a4-f983-4168-8f27-85436447bb57")
    private String recordKey;
}