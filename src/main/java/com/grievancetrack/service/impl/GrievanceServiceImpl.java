package com.grievancetrack.service.impl;

import com.grievancetrack.dto.request.GrievanceCreateRequest;
import com.grievancetrack.dto.request.GrievanceStatusUpdateRequest;
import com.grievancetrack.dto.response.GrievanceResponse;
import com.grievancetrack.dto.response.PageResponse;
import com.grievancetrack.entity.Category;
import com.grievancetrack.entity.Department;
import com.grievancetrack.entity.Escalation;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.NotificationType;
import com.grievancetrack.exception.InvalidRequestException;
import com.grievancetrack.exception.InvalidStatusTransitionException;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.CategoryRepository;
import com.grievancetrack.repository.EscalationRepository;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.service.GrievanceService;
import com.grievancetrack.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class GrievanceServiceImpl implements GrievanceService {

    private static final Logger log = LoggerFactory.getLogger(GrievanceServiceImpl.class);

    private final GrievanceRepository grievanceRepository;
    private final CategoryRepository categoryRepository;
    private final EscalationRepository escalationRepository;
    private final NotificationService notificationService;

    public GrievanceServiceImpl(GrievanceRepository grievanceRepository,
                                CategoryRepository categoryRepository,
                                EscalationRepository escalationRepository,
                                NotificationService notificationService) {
        this.grievanceRepository = grievanceRepository;
        this.categoryRepository = categoryRepository;
        this.escalationRepository = escalationRepository;
        this.notificationService = notificationService;
    }

    private synchronized String generateNextComplaintCode() {
        long nextId = grievanceRepository.findTopByOrderByIdDesc()
                .map(g -> g.getId() + 1)
                .orElse(1L);

        String code = String.format("GRV-%06d", nextId);
        int attempts = 0;
        while (grievanceRepository.findByComplaintCodeIgnoreCase(code).isPresent() && attempts < 1000) {
            nextId++;
            code = String.format("GRV-%06d", nextId);
            attempts++;
        }
        return code;
    }

    @Override
    public GrievanceResponse createGrievance(GrievanceCreateRequest request) {
        // Feature 1 & 2: Category lookup and Automatic Department Routing
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Department must automatically be determined from the selected category
        Department department = category.getDepartment();
        if (department == null) {
            throw new InvalidRequestException("Selected category does not have an assigned department");
        }

        // SLA calculation: createdAt + category.slaDays
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime slaDeadline = createdAt.plusDays(category.getSlaDays());

        String complaintCode = generateNextComplaintCode();

        Grievance grievance = new Grievance(
                complaintCode,
                request.getCitizenName().trim(),
                request.getEmail().trim(),
                request.getPhone().trim(),
                request.getDescription().trim(),
                request.getLocation().trim(),
                category,
                department,
                createdAt,
                slaDeadline
        );

        Grievance saved = grievanceRepository.save(grievance);

        // Generate submission notification
        String msg = String.format("Grievance %s submitted successfully under Category '%s' and assigned to %s (SLA: %d days).",
                complaintCode, category.getName(), department.getName(), category.getSlaDays());
        notificationService.createNotification(saved, msg, NotificationType.ASSIGNMENT);

        log.info("New grievance created: code={} citizen={} dept={} slaDays={}",
                complaintCode, saved.getCitizenName(), department.getName(), category.getSlaDays());

        return GrievanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GrievanceResponse getGrievanceById(Long id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found with ID: " + id));
        return GrievanceResponse.fromEntity(grievance);
    }

    @Override
    @Transactional(readOnly = true)
    public GrievanceResponse getGrievanceByCode(String complaintCode) {
        Grievance grievance = grievanceRepository.findByComplaintCodeIgnoreCase(complaintCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No grievance found with complaint code: " + complaintCode));
        return GrievanceResponse.fromEntity(grievance);
    }

    @Override
    public GrievanceResponse updateStatus(Long id, GrievanceStatusUpdateRequest request) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found with ID: " + id));

        GrievanceStatus currentStatus = grievance.getStatus();
        GrievanceStatus targetStatus = request.getStatus();

        // Feature 3: Enforce valid status transitions
        if (!currentStatus.canTransitionTo(targetStatus)) {
            String errorMsg = String.format("Invalid status transition from %s to %s. Allowed transitions from %s: %s",
                    currentStatus, targetStatus, currentStatus, currentStatus.getAllowedNextStatuses());
            throw new InvalidStatusTransitionException(errorMsg);
        }

        grievance.setStatus(targetStatus);

        if (targetStatus == GrievanceStatus.RESOLVED) {
            grievance.setResolvedAt(LocalDateTime.now());
        }

        // If manually escalated by officer
        if (targetStatus == GrievanceStatus.ESCALATED && grievance.getEscalation() == null) {
            String officer = (request.getOfficerName() != null && !request.getOfficerName().isBlank())
                    ? request.getOfficerName().trim()
                    : "Zonal Senior Supervisory Officer";
            String reason = (request.getRemarks() != null && !request.getRemarks().isBlank())
                    ? request.getRemarks().trim()
                    : "Administrative escalation by department officer";

            Escalation escalation = new Escalation(grievance, officer, LocalDateTime.now(), reason);
            Escalation savedEscalation = escalationRepository.save(escalation);
            grievance.setEscalation(savedEscalation);
        }

        Grievance updated = grievanceRepository.save(grievance);

        // Generate status change notification
        String remarksSuffix = (request.getRemarks() != null && !request.getRemarks().isBlank())
                ? " (Remarks: " + request.getRemarks() + ")"
                : "";
        String msg = String.format("Grievance %s status changed from %s to %s%s.",
                grievance.getComplaintCode(), currentStatus, targetStatus, remarksSuffix);
        notificationService.createNotification(updated, msg, NotificationType.STATUS_CHANGE);

        log.info("Status transition: code={} from={} to={}", grievance.getComplaintCode(), currentStatus, targetStatus);
        return GrievanceResponse.fromEntity(updated);
    }

    @Override
    public void deleteGrievance(Long id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found with ID: " + id));
        grievanceRepository.delete(grievance);
        log.info("Grievance deleted: id={} code={}", id, grievance.getComplaintCode());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GrievanceResponse> getGrievances(String search, GrievanceStatus status,
                                                        Long categoryId, Long departmentId, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Grievance> page = grievanceRepository.findWithFilters(cleanSearch, status, categoryId, departmentId, pageable);
        return PageResponse.of(page.map(GrievanceResponse::fromEntity));
    }
}
