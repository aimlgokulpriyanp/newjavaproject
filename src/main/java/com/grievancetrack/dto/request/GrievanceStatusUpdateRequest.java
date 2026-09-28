package com.grievancetrack.dto.request;

import com.grievancetrack.entity.enums.GrievanceStatus;
import jakarta.validation.constraints.NotNull;

public class GrievanceStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private GrievanceStatus status;

    private String remarks;

    private String officerName;

    public GrievanceStatusUpdateRequest() {
    }

    public GrievanceStatusUpdateRequest(GrievanceStatus status, String remarks, String officerName) {
        this.status = status;
        this.remarks = remarks;
        this.officerName = officerName;
    }

    public GrievanceStatus getStatus() {
        return status;
    }

    public void setStatus(GrievanceStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }
}
