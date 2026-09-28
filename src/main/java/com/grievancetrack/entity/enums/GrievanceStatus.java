package com.grievancetrack.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Defined stages for Grievance lifecycle:
 * PENDING -> ASSIGNED -> IN_PROGRESS -> RESOLVED / ESCALATED / REJECTED
 */
public enum GrievanceStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    ESCALATED,
    REJECTED;

    private static final Map<GrievanceStatus, Set<GrievanceStatus>> VALID_TRANSITIONS = Map.of(
            PENDING, EnumSet.of(ASSIGNED, REJECTED),
            ASSIGNED, EnumSet.of(IN_PROGRESS, REJECTED),
            IN_PROGRESS, EnumSet.of(RESOLVED, ESCALATED, REJECTED),
            ESCALATED, EnumSet.of(RESOLVED, REJECTED),
            RESOLVED, Collections.emptySet(),
            REJECTED, Collections.emptySet()
    );

    public boolean canTransitionTo(GrievanceStatus nextStatus) {
        if (nextStatus == null || nextStatus == this) {
            return false;
        }
        Set<GrievanceStatus> allowed = VALID_TRANSITIONS.getOrDefault(this, Collections.emptySet());
        return allowed.contains(nextStatus);
    }

    public Set<GrievanceStatus> getAllowedNextStatuses() {
        return VALID_TRANSITIONS.getOrDefault(this, Collections.emptySet());
    }

    public boolean isTerminal() {
        return this == RESOLVED || this == REJECTED;
    }
}
