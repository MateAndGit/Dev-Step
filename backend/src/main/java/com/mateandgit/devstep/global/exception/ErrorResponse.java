package com.mateandgit.devstep.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private int status;
    private String code;
    private String message;

    public ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus(), errorCode.name(), errorCode.getMessage());
    }
}
