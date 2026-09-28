package com.grievancetrack.dto.response;

import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.SlaStatus;
import java.time.LocalDateTime;
import java.util.Set;

public class GrievanceResponse {
    private Long id;
    private String complaintCode;
    private String citizenName;
    private String email;
    private String phone;
    private String description;
    private String location;
    private GrievanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;
    private CategoryResponse category;
    private DepartmentResponse department;
    private SlaStatus slaStatus;
    private Integer slaDays;
    private EscalationResponse escalation;
    private RatingResponse rating;
    private Set<GrievanceStatus> allowedNextStatuses;

    public GrievanceResponse() {
    }

    public static GrievanceResponse fromEntity(Grievance g) {
        if (g == null) return null;
        GrievanceResponse dto = new GrievanceResponse();
        dto.setId(g.getId());
        dto.setComplaintCode(g.getComplaintCode());
        dto.setCitizenName(g.getCitizenName());
        dto.setEmail(g.getEmail());
        dto.setPhone(g.getPhone());
        dto.setDescription(g.getDescription());
        dto.setLocation(g.getLocation());
        dto.setStatus(g.getStatus());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        dto.setSlaDeadline(g.getSlaDeadline());
        dto.setResolvedAt(g.getResolvedAt());
        dto.setSlaStatus(g.getSlaStatus());
        dto.setAllowedNextStatuses(g.getStatus().getAllowedNextStatuses());

        if (g.getCategory() != null) {
            dto.setCategory(CategoryResponse.fromEntity(g.getCategory()));
            dto.setSlaDays(g.getCategory().getSlaDays());
        }

        if (g.getDepartment() != null) {
            dto.setDepartment(DepartmentResponse.fromEntity(g.getDepartment()));
        }

        if (g.getEscalation() != null) {
            dto.setEscalation(EscalationResponse.fromEntity(g.getEscalation()));
        }

        if (g.getRating() != null) {
            dto.setRating(RatingResponse.fromEntity(g.getRating()));
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplaintCode() {
        return complaintCode;
    }

    public void setComplaintCode(String complaintCode) {
        this.complaintCode = complaintCode;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public GrievanceStatus getStatus() {
        return status;
    }

    public void setStatus(GrievanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public DepartmentResponse getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentResponse department) {
        this.department = department;
    }

    public SlaStatus getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(SlaStatus slaStatus) {
        this.slaStatus = slaStatus;
    }

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
    }

    public EscalationResponse getEscalation() {
        return escalation;
    }

    public void setEscalation(EscalationResponse escalation) {
        this.escalation = escalation;
    }

    public RatingResponse getRating() {
        return rating;
    }

    public void setRating(RatingResponse rating) {
        this.rating = rating;
    }

    public Set<GrievanceStatus> getAllowedNextStatuses() {
        return allowedNextStatuses;
    }

    public void setAllowedNextStatuses(Set<GrievanceStatus> allowedNextStatuses) {
        this.allowedNextStatuses = allowedNextStatuses;
    }
}
