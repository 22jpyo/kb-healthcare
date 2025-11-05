package com.kb.healthcare.dto.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "헬스 데이터 업로드 요청")
public record HealthUploadRequest(
        @Schema(description = "건강 기록 키", example = "3b87c9a4-f983-4168-8f27-85436447bb57")
        @JsonProperty("recordkey")
        String recordKey,

        @Schema(description = "헬스 데이터 목록")
        Data data,

        @Schema(description = "마지막 업데이트 시간 (ISO 8601 형식)", example = "2025-11-05 10:30:00")
        String lastUpdate
) {
    @Schema(description = "헬스 데이터 컨테이너")
    public record Data(
            @Schema(description = "헬스 데이터 항목 리스트")
            List<Entry> entries
    ) {
    }

    @Schema(description = "개별 헬스 데이터 항목")
    public record Entry(
            @Schema(description = "측정 기간")
            Period period,

            @Schema(description = "이동 거리 정보")
            Metric distance,

            @Schema(description = "소모 칼로리 정보")
            Metric calories,

            @Schema(description = "걸음 수", example = "10000.0")
            Double steps
    ) {
    }

    @Schema(description = "측정 기간")
    public record Period(
            @Schema(description = "시작 시간", example = "2025-11-05 10:30:00")
            String from,

            @Schema(description = "종료 시간", example = "2025-11-05 10:40:00")
            String to
    ) {
    }

    @Schema(description = "측정값 (단위 포함)")
    public record Metric(
            @Schema(description = "측정 단위", example = "km", allowableValues = {"km", "kcal"})
            String unit,

            @Schema(description = "측정값", example = "7.5")
            BigDecimal value
    ) {
    }
}
