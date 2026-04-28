package com.rikai.backend.model.Enum;

/**
 * Defines how deep the AI should expand a roadmap node.
 */
public enum ExpansionDepth {
    /**
     * Expand only to MODULE level (PHASE → MODULE).
     */
    MODULES_ONLY,

    /**
     * Expand to LESSON level (PHASE → MODULE → LESSON).
     */
    LESSONS_ONLY,

    /**
     * Full expansion to TASK level (PHASE → MODULE → LESSON → TASK).
     */
    FULL_DEPTH
}
