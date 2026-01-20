package com.rikai.backend.model.Enum;

import lombok.Getter;

@Getter
public enum CriteriaCategory {
    WORK_PERFORMANCE("I. WORK PERFORMANCE", "Work Performance"),
    ATTITUDE_SOFT_SKILLS("II. ATTITUDE AND SOFT SKILLS", "Attitude and Soft Skills"),
    KNOWLEDGE_APPLICATION("III. KNOWLEDGE APPLICATION", "Knowledge Application");

    private final String displayName;
    private final String description;

    CriteriaCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
