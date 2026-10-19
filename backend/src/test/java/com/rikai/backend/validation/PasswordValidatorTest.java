package com.rikai.backend.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Admin@123",
            "Password1",
            "SecurePass99",
            "P@ssw0rd2026!"
    })
    @DisplayName("Valid passwords containing uppercase, lowercase, digit, and length >= 8 return true")
    void isValid_ValidPassword_ReturnsTrue(String password) {
        assertTrue(PasswordValidator.isValid(password));
    }

    @Test
    @DisplayName("Null password returns false")
    void isValid_NullPassword_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",             // empty
            "   ",          // blank
            "Aa1!",         // too short (< 8 chars)
            "Abcdef1",      // 7 chars
            "password123",  // missing uppercase
            "PASSWORD123",  // missing lowercase
            "PasswordOnly"  // missing digit
    })
    @DisplayName("Invalid passwords return false")
    void isValid_InvalidPassword_ReturnsFalse(String password) {
        assertFalse(PasswordValidator.isValid(password));
    }
}

