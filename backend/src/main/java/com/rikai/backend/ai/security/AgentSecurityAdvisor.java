package com.rikai.backend.ai.security;

import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AgentSecurityAdvisor {

    AuthenticationService authenticationService;
    public boolean canAccessIntern(Long internId) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";

        if ("ADMIN".equals(roleName) || "HR".equals(roleName)) {
            return true;
        }

        return "MENTOR".equals(roleName);
    }

    public boolean isAdminOrHR() {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }

        String roleName = currentUser.getRole().getRoleName();
        return "ADMIN".equals(roleName) || "HR".equals(roleName);
    }

    public String getCurrentMentorId() {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return null;
        }

        if ("MENTOR".equals(currentUser.getRole().getRoleName())) {
            return currentUser.getId().toString();
        }

        return null;
    }
}
