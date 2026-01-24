package com.rikai.backend.validation;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_REGEX);

    private PasswordValidator() {
    }

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PATTERN.matcher(password).matches();
    }
}