package com.kb.healthcare.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "월간 헬스 데이터 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthMonthlyResponse implements Serializable {
    @Schema(description = "연-월 (YYYY-MM 형식)", example = "2025-11")
    String monthly;

    @Schema(description = "월간 총 걸음 수", example = "300000")
    Integer steps;

    @Schema(description = "월간 총 소모 칼로리 (kcal)", example = "10500.5")
    BigDecimal calories;

    @Schema(description = "월간 총 이동 거리 (km)", example = "225.0")
    BigDecimal distance;

    @Schema(description = "건강 기록 키", example = "3b87c9a4-f983-4168-8f27-85436447bb57")
    String recordKey;
}