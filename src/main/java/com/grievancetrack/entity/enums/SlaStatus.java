package com.grievancetrack.entity.enums;

import java.time.LocalDateTime;

/**
 * SLA Status states:
 * ON_TRACK, DUE_SOON, SLA_BREACHED, ESCALATED, COMPLETED
 */
public enum SlaStatus {
    ON_TRACK("On Track", "Within SLA timeline"),
    DUE_SOON("Due Soon", "Approaching SLA deadline within 24 hours"),
    SLA_BREACHED("SLA Breached", "SLA deadline exceeded"),
    ESCALATED("Escalated", "Escalated to senior officer due to SLA breach"),
    COMPLETED("Completed", "Resolved within or after SLA");

    private final String displayName;
    private final String description;

    SlaStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static SlaStatus compute(GrievanceStatus grievanceStatus, LocalDateTime slaDeadline, LocalDateTime now) {
        if (grievanceStatus == GrievanceStatus.RESOLVED) {
            return COMPLETED;
        }
        if (grievanceStatus == GrievanceStatus.ESCALATED) {
            return ESCALATED;
        }
        if (slaDeadline == null) {
            return ON_TRACK;
        }
        LocalDateTime current = (now != null) ? now : LocalDateTime.now();
        if (current.isAfter(slaDeadline)) {
            return SLA_BREACHED;
        }
        // If deadline is within next 24 hours
        if (current.plusHours(24).isAfter(slaDeadline)) {
            return DUE_SOON;
        }
        return ON_TRACK;
    }
}
