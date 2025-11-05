package com.kb.healthcare.dto.health;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record HealthUploadRequest(
        @JsonProperty("recordkey") String recordKey,
        Data data,
        String lastUpdate
) {
    public record Data(
            List<Entry> entries
    ) {
    }

    public record Entry(
            Period period,
            Metric distance,
            Metric calories,
            Double steps
    ) {
    }

    public record Period(
            String from,
            String to
    ) {
    }

    public record Metric(
            String unit,
            BigDecimal value
    ) {
    }
}
