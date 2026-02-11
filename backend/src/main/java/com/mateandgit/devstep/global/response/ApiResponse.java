package com.mateandgit.devstep.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mateandgit.devstep.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private ApiResponse() {
        this.success = true;
        this.code = "SUCCESS";
        this.message = null;
        this.data = null;
    }

    private ApiResponse(T data) {
        this.success = true;
        this.code = "SUCCESS";
        this.message = null;
        this.data = data;
    }

    private ApiResponse(ErrorCode errorCode) {
        this.success = false;
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = null;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static ApiResponse<?> success() {
        return new ApiResponse<>();
    }

    public static ApiResponse<?> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode);
    }
}