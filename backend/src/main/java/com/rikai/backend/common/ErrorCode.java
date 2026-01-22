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
    PASSWORD_WEAK("Password must be at least 8 characters long and include uppercase, lowercase, and numbers.",
            HttpStatus.BAD_REQUEST),
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
    INTERN_NOT_EXISTED("Intern not found", HttpStatus.NOT_FOUND),
    JOB_POSITION_NOT_EXISTED("Job position not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_NOT_EXISTED("Department not found", HttpStatus.NOT_FOUND),
    POSITION_NOT_EXISTED("Position not found", HttpStatus.NOT_FOUND),
    MENTOR_NOT_EXISTED("Mentor not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED("Role not found", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE("End date must be after start date", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    USERNAME_OR_PASSWORD_INCORRECT("Incorrect username or password.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("You do not have permission", HttpStatus.FORBIDDEN),
    // Weekly Report errors
    WEEKLY_REPORT_NOT_EXISTED("Weekly report not found", HttpStatus.NOT_FOUND),
    WEEKLY_REPORT_DUPLICATE("Weekly report already exists for this intern and week", HttpStatus.BAD_REQUEST),
    INTERN_ID_REQUIRED("Intern ID is required", HttpStatus.BAD_REQUEST),
    WEEK_START_DATE_REQUIRED("Week start date is required", HttpStatus.BAD_REQUEST),
    INVALID_SCORE("Score must be between 1 and 10", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_INTERN_ACCESS("You do not have permission to access this intern", HttpStatus.FORBIDDEN),

    EVALUATION_CRITERIA_NOT_EXISTED("Evaluation criteria not found", HttpStatus.NOT_FOUND),
    CRITERIA_SCORE_DEFINITION_NOT_EXISTED("Criteria score definition not found", HttpStatus.NOT_FOUND),
    INVALID_CRITERIA_PARENT("Invalid parent criteria", HttpStatus.BAD_REQUEST),
    INVALID_CRITERIA_CATEGORY("Criteria category does not match parent category", HttpStatus.BAD_REQUEST),

    // Validation keys (DTO)
    CRITERIA_CATEGORY_REQUIRED("Criteria category is required", HttpStatus.BAD_REQUEST),
    CRITERIA_NAME_REQUIRED("Criteria name is required", HttpStatus.BAD_REQUEST),
    DISPLAY_ORDER_INVALID("Display order must be >= 0", HttpStatus.BAD_REQUEST),
    CRITERIA_ID_REQUIRED("Criteria id is required", HttpStatus.BAD_REQUEST),
    SCORE_LABEL_REQUIRED("Score label is required", HttpStatus.BAD_REQUEST),
    INVALID_FULLNAME("Full name is required", HttpStatus.BAD_REQUEST),
    ROLE_REQUIRED("Role is required", HttpStatus.BAD_REQUEST),
    DEPARTMENT_REQUIRED("Department is required", HttpStatus.BAD_REQUEST),
    POSITION_TITLE_REQUIRED("Position title is required", HttpStatus.BAD_REQUEST),
    JOB_POSITION_REQUIRED("Job position is required", HttpStatus.BAD_REQUEST),
    MENTOR_REQUIRED("Mentor is required", HttpStatus.BAD_REQUEST),
    START_DATE_REQUIRED("Start date is required", HttpStatus.BAD_REQUEST),
    END_DATE_REQUIRED("End date is required", HttpStatus.BAD_REQUEST),
    STATUS_REQUIRED("Status is required", HttpStatus.BAD_REQUEST),
    DEPARTMENT_TITLE_REQUIRED("Department title is required", HttpStatus.BAD_REQUEST),

    FORBIDDEN("You are not allowed to perform this action", HttpStatus.FORBIDDEN),
    INVALID_INTERN_STATUS("Invalid intern status", HttpStatus.BAD_REQUEST),

    // Evaluation Session errors
    EVALUATION_SESSION_NOT_EXISTED("Evaluation session not found", HttpStatus.NOT_FOUND),
    EVALUATION_SESSION_ALREADY_EXISTS("Evaluation session already exists for this intern and session type", HttpStatus.BAD_REQUEST),
    EVALUATION_SESSION_SEQUENCE_INVALID("Previous evaluation session must be completed first", HttpStatus.BAD_REQUEST),
    INVALID_SESSION_TYPE("Invalid session type", HttpStatus.BAD_REQUEST),
    INVALID_EVALUATION_DATE("Evaluation date must be within intern's internship period", HttpStatus.BAD_REQUEST),
    NO_WEEKLY_REPORTS_FOUND("No weekly reports found for the evaluation period", HttpStatus.BAD_REQUEST),
    SESSION_TYPE_REQUIRED("Session type is required", HttpStatus.BAD_REQUEST),
    EVALUATION_DATE_REQUIRED("Evaluation date is required", HttpStatus.BAD_REQUEST);

    ErrorCode(String message, HttpStatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    private final String message;
    private final HttpStatusCode statusCode;
}
