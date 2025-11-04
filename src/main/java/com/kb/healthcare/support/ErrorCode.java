package com.kb.healthcare.support;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_RECORD_KEY(HttpStatus.CONFLICT, "이미 사용 중인 recordKey 입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    public final HttpStatus status;
    public final String message;

    ErrorCode(HttpStatus s, String m) {
        this.status = s;
        this.message = m;
    }
}