package com.grievancetrack.service;

import com.grievancetrack.dto.response.EscalationResponse;
import com.grievancetrack.entity.Grievance;

import java.util.List;

public interface EscalationService {
    List<EscalationResponse> getAllEscalations();
    EscalationResponse getEscalationById(Long id);
    EscalationResponse getEscalationByGrievanceId(Long grievanceId);
    EscalationResponse escalateGrievance(Grievance grievance, String seniorOfficerName, String reason);
}
