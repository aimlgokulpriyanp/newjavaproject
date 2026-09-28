package com.grievancetrack.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    @NotBlank(message = "Officer name is required")
    private String officerName;

    @NotBlank(message = "Contact information is required")
    private String contact;

    public DepartmentRequest() {
    }

    public DepartmentRequest(String name, String officerName, String contact) {
        this.name = name;
        this.officerName = officerName;
        this.contact = contact;
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
}
