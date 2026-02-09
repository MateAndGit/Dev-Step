package com.mateandgit.devstep.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "C003", "Server Error"),

    // User
    USER_NOT_FOUND(404, "U001", "User not found"),
    DUPLICATE_NICKNAME(409, "U002", "Duplicate Nickname"),
    INVALID_NICKNAME_FORMAT(400, "U003", "Invalid Nickname Format"),
    BANNED_NICKNAME(400, "U004", "Nickname contains inappropriate words"),
    DUPLICATE_EMAIL(409, "U005", "Duplicate Email"),
    INVALID_EMAIL_FORMAT(400, "U006", "Invalid Email Format");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

