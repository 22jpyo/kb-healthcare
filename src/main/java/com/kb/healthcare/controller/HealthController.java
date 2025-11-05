package com.kb.healthcare.controller;

import com.kb.healthcare.dto.health.HealthDailyResponse;
import com.kb.healthcare.dto.health.HealthMonthlyResponse;
import com.kb.healthcare.dto.health.HealthUploadRequest;
import com.kb.healthcare.dto.health.HealthUploadResponse;
import com.kb.healthcare.security.CustomUserDetails;
import com.kb.healthcare.service.HealthService;
import com.kb.healthcare.support.CustomException;
import com.kb.healthcare.support.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @PostMapping("/upload")
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
    public ResponseEntity<List<HealthDailyResponse>> getDaily(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(healthService.getDaily(user.getRecordKey()));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<HealthMonthlyResponse>> getMonthly(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(healthService.getMonthly(user.getRecordKey()));
    }
}