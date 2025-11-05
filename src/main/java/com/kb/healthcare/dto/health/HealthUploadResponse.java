package com.kb.healthcare.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "헬스 데이터 업로드 응답")
public record HealthUploadResponse(
        @Schema(description = "건강 기록 키", example = "3b87c9a4-f983-4168-8f27-85436447bb57")
        String recordkey,
        
        @Schema(description = "성공적으로 저장/업데이트된 데이터 항목 개수", example = "10")
        Integer ingested
) {
}