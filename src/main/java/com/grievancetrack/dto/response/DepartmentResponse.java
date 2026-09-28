package com.grievancetrack.dto.response;

import com.grievancetrack.entity.Department;
import java.time.LocalDateTime;

public class DepartmentResponse {
    private Long id;
    private String name;
    private String officerName;
    private String contact;
    private LocalDateTime createdAt;
    private int categoriesCount;
    private int grievancesCount;

    public DepartmentResponse() {
    }

    public static DepartmentResponse fromEntity(Department department) {
        if (department == null) return null;
        DepartmentResponse dto = new DepartmentResponse();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setOfficerName(department.getOfficerName());
        dto.setContact(department.getContact());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setCategoriesCount(department.getCategories() != null ? department.getCategories().size() : 0);
        dto.setGrievancesCount(department.getGrievances() != null ? department.getGrievances().size() : 0);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(int categoriesCount) {
        this.categoriesCount = categoriesCount;
    }

    public int getGrievancesCount() {
        return grievancesCount;
    }

    public void setGrievancesCount(int grievancesCount) {
        this.grievancesCount = grievancesCount;
    }
}
