package com.grievancetrack.dto.response;

import com.grievancetrack.entity.Escalation;
import java.time.LocalDateTime;

public class EscalationResponse {
    private Long id;
    private Long grievanceId;
    private String complaintCode;
    private String seniorOfficerName;
    private LocalDateTime escalatedAt;
    private String reason;

    public EscalationResponse() {
    }

    public static EscalationResponse fromEntity(Escalation escalation) {
        if (escalation == null) return null;
        EscalationResponse dto = new EscalationResponse();
        dto.setId(escalation.getId());
        if (escalation.getGrievance() != null) {
            dto.setGrievanceId(escalation.getGrievance().getId());
            dto.setComplaintCode(escalation.getGrievance().getComplaintCode());
        }
        dto.setSeniorOfficerName(escalation.getSeniorOfficerName());
        dto.setEscalatedAt(escalation.getEscalatedAt());
        dto.setReason(escalation.getReason());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(Long grievanceId) {
        this.grievanceId = grievanceId;
    }

    public String getComplaintCode() {
        return complaintCode;
    }

    public void setComplaintCode(String complaintCode) {
        this.complaintCode = complaintCode;
    }

    public String getSeniorOfficerName() {
        return seniorOfficerName;
    }

    public void setSeniorOfficerName(String seniorOfficerName) {
        this.seniorOfficerName = seniorOfficerName;
    }

    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
