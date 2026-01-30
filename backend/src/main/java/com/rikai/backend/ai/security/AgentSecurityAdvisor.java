package com.rikai.backend.ai.security;

import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AgentSecurityAdvisor {

    AuthenticationService authenticationService;
    InternRepository internRepository;

    public boolean canAccessIntern(Long internId) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";

        if ("ADMIN".equals(roleName) || "HR".equals(roleName)) {
            return true;
        }

        if ("MENTOR".equals(roleName)) {
            Optional<Intern> internOpt = internRepository.findById(internId);
            if (internOpt.isEmpty() || internOpt.get().getMentor() == null) {
                return false;
            }
            return internOpt.get().getMentor().getId().equals(currentUser.getId());
        }
        return false;
    }

    public boolean isAdminOrHR() {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        String roleName = currentUser.getRole().getRoleName();
        return "ADMIN".equals(roleName) || "HR".equals(roleName);
    }

    public UUID getCurrentMentorId() {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != null
                && "MENTOR".equals(currentUser.getRole().getRoleName())) {
            return currentUser.getId();
        }
        return null;
    }
}