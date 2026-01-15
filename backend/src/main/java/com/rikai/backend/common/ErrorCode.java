package com.rikai.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("Unknown error.", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY("Invalid error message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED("User already exists", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME("Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_WEAK("Password must be at least 8 characters long and include uppercase, lowercase, and numbers.", HttpStatus.BAD_REQUEST),
    INVALID_DOB("Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_FIRSTNAME("First name cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_LASTNAME("Last name cannot be empty", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_CORRECT("Old password is not correct", HttpStatus.BAD_REQUEST),

    URL_NOT_FOUND("URL not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("HTTP Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),

    USER_NOT_EXISTED("User not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXISTED("Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_EXISTED("Product not found", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),

    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    USERNAME_OR_PASSWORD_INCORRECT("Incorrect username or password.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("You do not have permission", HttpStatus.FORBIDDEN),
    DEPARTMENT_NOT_EXISTED("Department not found", HttpStatus.NOT_FOUND),
    
    // Weekly Report errors
    INTERN_NOT_EXISTED("Intern not found", HttpStatus.NOT_FOUND),
    WEEKLY_REPORT_NOT_EXISTED("Weekly report not found", HttpStatus.NOT_FOUND),
    WEEKLY_REPORT_DUPLICATE("Weekly report already exists for this intern and week", HttpStatus.BAD_REQUEST),
    INTERN_ID_REQUIRED("Intern ID is required", HttpStatus.BAD_REQUEST),
    WEEK_START_DATE_REQUIRED("Week start date is required", HttpStatus.BAD_REQUEST),
    INVALID_SCORE("Score must be between 1 and 10", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_INTERN_ACCESS("You do not have permission to access this intern", HttpStatus.FORBIDDEN),
    ;

    ErrorCode(String message, HttpStatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    private final String message;
    private final HttpStatusCode statusCode;
}
