package com.grievancetrack.service.impl;

import com.grievancetrack.dto.response.EscalationResponse;
import com.grievancetrack.entity.Escalation;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.NotificationType;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.EscalationRepository;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.service.EscalationService;
import com.grievancetrack.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EscalationServiceImpl implements EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationServiceImpl.class);

    private final EscalationRepository escalationRepository;
    private final GrievanceRepository grievanceRepository;
    private final NotificationService notificationService;

    public EscalationServiceImpl(EscalationRepository escalationRepository,
                                 GrievanceRepository grievanceRepository,
                                 NotificationService notificationService) {
        this.escalationRepository = escalationRepository;
        this.grievanceRepository = grievanceRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscalationResponse> getAllEscalations() {
        return escalationRepository.findAllByOrderByEscalatedAtDesc().stream()
                .map(EscalationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EscalationResponse getEscalationById(Long id) {
        Escalation esc = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found with ID: " + id));
        return EscalationResponse.fromEntity(esc);
    }

    @Override
    @Transactional(readOnly = true)
    public EscalationResponse getEscalationByGrievanceId(Long grievanceId) {
        Escalation esc = escalationRepository.findByGrievanceId(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("No escalation record found for Grievance ID: " + grievanceId));
        return EscalationResponse.fromEntity(esc);
    }

    @Override
    public EscalationResponse escalateGrievance(Grievance grievance, String seniorOfficerName, String reason) {
        if (escalationRepository.existsByGrievanceId(grievance.getId())) {
            log.warn("Grievance {} is already escalated. Skipping duplicate escalation.", grievance.getComplaintCode());
            return EscalationResponse.fromEntity(escalationRepository.findByGrievanceId(grievance.getId()).orElse(null));
        }

        LocalDateTime now = LocalDateTime.now();
        Escalation escalation = new Escalation(grievance, seniorOfficerName, now, reason);
        Escalation saved = escalationRepository.save(escalation);

        grievance.setStatus(GrievanceStatus.ESCALATED);
        grievance.setEscalation(saved);
        grievanceRepository.save(grievance);

        String message = String.format("Grievance %s has been automatically ESCALATED to Senior Officer %s. Reason: %s",
                grievance.getComplaintCode(), seniorOfficerName, reason);
        notificationService.createNotification(grievance, message, NotificationType.SLA_ESCALATION);

        log.info("AUTOMATIC SLA ESCALATION: Grievance {} escalated to {}", grievance.getComplaintCode(), seniorOfficerName);
        return EscalationResponse.fromEntity(saved);
    }
}
