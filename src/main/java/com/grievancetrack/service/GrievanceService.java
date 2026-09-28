package com.grievancetrack.service;

import com.grievancetrack.dto.request.GrievanceCreateRequest;
import com.grievancetrack.dto.request.GrievanceStatusUpdateRequest;
import com.grievancetrack.dto.response.GrievanceResponse;
import com.grievancetrack.dto.response.PageResponse;
import com.grievancetrack.entity.enums.GrievanceStatus;
import org.springframework.data.domain.Pageable;

public interface GrievanceService {
    GrievanceResponse createGrievance(GrievanceCreateRequest request);
    GrievanceResponse getGrievanceById(Long id);
    GrievanceResponse getGrievanceByCode(String complaintCode);
    GrievanceResponse updateStatus(Long id, GrievanceStatusUpdateRequest request);
    void deleteGrievance(Long id);
    PageResponse<GrievanceResponse> getGrievances(String search, GrievanceStatus status, Long categoryId, Long departmentId, Pageable pageable);
}
