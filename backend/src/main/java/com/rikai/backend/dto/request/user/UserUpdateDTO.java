package com.rikai.backend.dto.request.user;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String email;
    private String password;
    private String fullName;
    private LocalDate dateOfBirth;
    private boolean isActive;
    private Integer departmentId;
}
