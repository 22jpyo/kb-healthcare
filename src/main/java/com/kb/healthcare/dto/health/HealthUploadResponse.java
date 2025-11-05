package com.kb.healthcare.dto.health;

public record HealthUploadResponse(
        String recordkey,
        Integer ingested
) {
}