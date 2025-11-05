package com.kb.healthcare.controller;

import com.kb.healthcare.dto.health.HealthDailyResponse;
import com.kb.healthcare.dto.health.HealthMonthlyResponse;
import com.kb.healthcare.dto.health.HealthUploadRequest;
import com.kb.healthcare.dto.health.HealthUploadResponse;
import com.kb.healthcare.security.CustomUserDetails;
import com.kb.healthcare.service.HealthService;
import com.kb.healthcare.support.CustomException;
import com.kb.healthcare.support.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "헬스 데이터 업로드 및 조회 API")
public class HealthController {

    private final HealthService healthService;

    @PostMapping("/upload")
    @Operation(summary = "헬스 데이터 업로드", description = "사용자의 헬스 데이터를 서버로 업로드합니다.")
    public ResponseEntity<HealthUploadResponse> upload(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody HealthUploadRequest req
    ) {
        if (!user.getRecordKey().equals(req.recordKey())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        int count = healthService.upload(req);
        return ResponseEntity.ok(new HealthUploadResponse(req.recordKey(), count));
    }

    @GetMapping("/daily")
    @Operation(summary = "일간 헬스 데이터 조회", description = "사용자의 하루 단위 헬스 데이터를 조회합니다.")
    public ResponseEntity<List<HealthDailyResponse>> getDaily(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(healthService.getDaily(user.getRecordKey()));
    }

    @GetMapping("/monthly")
    @Operation(summary = "월간 헬스 데이터 조회", description = "사용자의 하루 단위 헬스 데이터를 조회합니다.")
    public ResponseEntity<List<HealthMonthlyResponse>> getMonthly(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(healthService.getMonthly(user.getRecordKey()));
    }
}