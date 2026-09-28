package com.grievancetrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalations")
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grievance_id", nullable = false, unique = true)
    private Grievance grievance;

    @NotBlank(message = "Senior officer name is required")
    @Column(name = "senior_officer_name", nullable = false, length = 150)
    private String seniorOfficerName;

    @Column(name = "escalated_at", nullable = false)
    private LocalDateTime escalatedAt = LocalDateTime.now();

    @NotBlank(message = "Escalation reason is required")
    @Column(nullable = false, length = 255)
    private String reason;

    public Escalation() {
    }

    public Escalation(Grievance grievance, String seniorOfficerName, LocalDateTime escalatedAt, String reason) {
        this.grievance = grievance;
        this.seniorOfficerName = seniorOfficerName;
        this.escalatedAt = escalatedAt;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Grievance getGrievance() {
        return grievance;
    }

    public void setGrievance(Grievance grievance) {
        this.grievance = grievance;
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
