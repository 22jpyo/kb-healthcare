package com.kb.healthcare.support;

public class CustomException extends RuntimeException {
    private final ErrorCode code;

    public CustomException(ErrorCode code) {
        super(code.message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}