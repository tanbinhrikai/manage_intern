package com.rikai.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum SuccessCode {
    LOGIN_SUCCESSFUL("Login successful", HttpStatus.OK),
    REFRESH_TOKEN_SUCCESSFUL("Refresh Token successful", HttpStatus.ACCEPTED),
    LOGOUT_SUCCESSFUL("Logout successful", HttpStatus.OK),
    GET_MY_INFO_SUCCESSFUL("Get my information successful", HttpStatus.OK),

    CREATE_USER_SUCCESSFUL("Create user successful", HttpStatus.CREATED),
    GET_USER_SUCCESSFUL("Get user successful", HttpStatus.OK),
    GET_ALL_USERS_SUCCESSFUL("Get all users successful", HttpStatus.OK),
    UPDATE_USER_SUCCESSFUL("Update user successful", HttpStatus.OK),
    DELETE_USER_SUCCESSFUL("Delete user successful", HttpStatus.OK),

    CREATE_CATEGORY_SUCCESSFUL("Create category successful", HttpStatus.CREATED),
    GET_CATEGORY_SUCCESSFUL("Get category successful", HttpStatus.OK),
    GET_ALL_CATEGORIES_SUCCESSFUL("Get all categories successful", HttpStatus.OK),
    UPDATE_CATEGORY_SUCCESSFUL("Update category successful", HttpStatus.OK),
    DELETE_CATEGORY_SUCCESSFUL("Delete category successful", HttpStatus.OK),

    CREATE_PRODUCT_SUCCESSFUL("Create product successful", HttpStatus.CREATED),
    GET_PRODUCT_SUCCESSFUL("Get product successful", HttpStatus.OK),
    GET_ALL_PRODUCTS_SUCCESSFUL("Get all products successful", HttpStatus.OK),
    UPDATE_PRODUCT_SUCCESSFUL("Update product successful", HttpStatus.OK),
    DELETE_PRODUCT_SUCCESSFUL("Delete product successful", HttpStatus.OK),

    CREATE_INTERN_SUCCESSFUL("Create intern successful", HttpStatus.CREATED),
    GET_INTERN_SUCCESSFUL("Get intern successful", HttpStatus.OK),
    GET_ALL_INTERNS_SUCCESSFUL("Get all interns successful", HttpStatus.OK),
    UPDATE_INTERN_SUCCESSFUL("Update intern successful", HttpStatus.OK),
    DELETE_INTERN_SUCCESSFUL("Delete intern successful", HttpStatus.OK),

    CHANGE_PASSWORD_SUCCESSFUL("Change password successful", HttpStatus.OK),

    GET_ALL_DEPARTMENTS_SUCCESSFUL("Get all departments successful", HttpStatus.OK),

    // Weekly Report success codes
    CREATE_WEEKLY_REPORT_SUCCESSFUL("Create weekly report successful", HttpStatus.CREATED),
    GET_WEEKLY_REPORT_SUCCESSFUL("Get weekly report successful", HttpStatus.OK),
    GET_ALL_WEEKLY_REPORTS_SUCCESSFUL("Get all weekly reports successful", HttpStatus.OK),
    UPDATE_WEEKLY_REPORT_SUCCESSFUL("Update weekly report successful", HttpStatus.OK),
    DELETE_WEEKLY_REPORT_SUCCESSFUL("Delete weekly report successful", HttpStatus.OK),

    // Intern success codes
    CREATE_DEPARTMENT_SUCCESSFUL("Create department successful", HttpStatus.CREATED),
    GET_DEPARTMENT_SUCCESSFUL("Get department successful", HttpStatus.OK),
    UPDATE_DEPARTMENT_SUCCESSFUL("Update department successful", HttpStatus.OK),

    CREATE_POSITION_SUCCESSFUL("Create position successful", HttpStatus.CREATED),
    GET_POSITION_SUCCESSFUL("Get position successful", HttpStatus.OK),
    GET_ALL_POSITIONS_SUCCESSFUL("Get all positions successful", HttpStatus.OK),
    UPDATE_POSITION_SUCCESSFUL("Update position successful", HttpStatus.OK),

    CREATE_EVALUATION_CRITERIA_SUCCESSFUL("Create evaluation criteria successful", HttpStatus.CREATED),
    GET_EVALUATION_CRITERIA_SUCCESSFUL("Get evaluation criteria successful", HttpStatus.OK),
    GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL("Get all evaluation criteria successful", HttpStatus.OK),
    UPDATE_EVALUATION_CRITERIA_SUCCESSFUL("Update evaluation criteria successful", HttpStatus.OK),
    DELETE_EVALUATION_CRITERIA_SUCCESSFUL("Delete evaluation criteria successful", HttpStatus.OK),

    CREATE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL("Create criteria score definition successful", HttpStatus.CREATED),
    GET_CRITERIA_SCORE_DEFINITION_SUCCESSFUL("Get criteria score definition successful", HttpStatus.OK),
    GET_ALL_CRITERIA_SCORE_DEFINITION_SUCCESSFUL("Get all criteria score definitions successful", HttpStatus.OK),
    UPDATE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL("Update criteria score definition successful", HttpStatus.OK),
    DELETE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL("Delete criteria score definition successful", HttpStatus.OK),

    CREATE_CRITERIA_GROUP_SUCCESSFUL("Create criteria group successful", HttpStatus.CREATED),
    GET_CRITERIA_GROUP_SUCCESSFUL("Get criteria group successful", HttpStatus.OK),
    UPDATE_CRITERIA_GROUP_SUCCESSFUL("Update criteria group successful", HttpStatus.OK),
    DELETE_CRITERIA_GROUP_SUCCESSFUL("Delete criteria group successful", HttpStatus.OK),

    // Evaluation Session success codes
    CREATE_EVALUATION_SESSION_SUCCESSFUL("Create evaluation session successful", HttpStatus.CREATED),
    GET_EVALUATION_SESSION_SUCCESSFUL("Get evaluation session successful", HttpStatus.OK),
    GET_ALL_EVALUATION_SESSIONS_SUCCESSFUL("Get all evaluation sessions successful", HttpStatus.OK),
    UPDATE_EVALUATION_SESSION_SUCCESSFUL("Update evaluation session successful", HttpStatus.OK),
    DELETE_EVALUATION_SESSION_SUCCESSFUL("Delete evaluation session successful", HttpStatus.OK),
    GENERATE_EVALUATION_SESSION_SUCCESSFUL("Generate evaluation session successful", HttpStatus.CREATED),
    GET_INTERN_EVALUATION_SUMMARY_SUCCESSFUL("Get intern evaluation summary successful", HttpStatus.OK),


    GET_ALL_INTERNSHIP_BATCHES_SUCCESSFUL("Get all internship batches successful", HttpStatus.OK),

    // Internship Batch success codes
    CREATE_BATCH_SUCCESSFUL("Create internship batch successful", HttpStatus.CREATED),
    GET_BATCH_SUCCESSFUL("Get internship batch successful", HttpStatus.OK),
    GET_ALL_BATCHES_SUCCESSFUL("Get all internship batches successful", HttpStatus.OK),
    UPDATE_BATCH_SUCCESSFUL("Update internship batch successful", HttpStatus.OK),
    DELETE_BATCH_SUCCESSFUL("Delete internship batch successful", HttpStatus.OK),
    
    // Dashboard success codes
    GET_RECENT_ACTIVITIES_SUCCESSFUL("Get recent activities successful", HttpStatus.OK),
    ;

    SuccessCode(String message, HttpStatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    private final String message;
    private final HttpStatusCode statusCode;
}