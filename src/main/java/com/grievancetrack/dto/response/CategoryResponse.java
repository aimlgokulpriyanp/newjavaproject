package com.grievancetrack.dto.response;

import com.grievancetrack.entity.Category;
import java.time.LocalDateTime;

public class CategoryResponse {
    private Long id;
    private String name;
    private Integer slaDays;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;

    public CategoryResponse() {
    }

    public static CategoryResponse fromEntity(Category category) {
        if (category == null) return null;
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlaDays(category.getSlaDays());
        if (category.getDepartment() != null) {
            dto.setDepartmentId(category.getDepartment().getId());
            dto.setDepartmentName(category.getDepartment().getName());
        }
        dto.setCreatedAt(category.getCreatedAt());
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

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
