package com.mateandgit.devstep.global.utils;

import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.exception.ErrorCode;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;
import java.util.regex.Pattern;

import static com.mateandgit.devstep.global.exception.ErrorCode.BANNED_NICKNAME;
import static com.mateandgit.devstep.global.exception.ErrorCode.INVALID_NICKNAME_FORMAT;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class ValidationUtils {

    //TODO After Implement Banned Words Logic
    private static final List<String> BANNED_WORDS = List.of("Banned_words1", "Banned_words1", "Banned_words1");

    public static void validateNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            throw new BusinessException(INVALID_NICKNAME_FORMAT);
        }

        // TODO if Banned Words a lot then this code is work well?
        // Aho-Corasick Algorithm
        String patternString = String.join("|", BANNED_WORDS);
        Pattern pattern = Pattern.compile(patternString, CASE_INSENSITIVE);

        if (pattern.matcher(nickname).find()) {
            throw new BusinessException(BANNED_NICKNAME);
        }
    }

    public static void validateEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        boolean isValid = EmailValidator.getInstance().isValid(email);
        if (!isValid) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

    }

    // TODO validatePostCreateRequest
    public static void validatePostCreateRequest(String title, String content) {
    }
}
