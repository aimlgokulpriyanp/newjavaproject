package com.grievancetrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "SLA days is required")
    @Positive(message = "SLA days must be positive")
    private Integer slaDays;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, Integer slaDays, Long departmentId) {
        this.name = name;
        this.slaDays = slaDays;
        this.departmentId = departmentId;
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
}
