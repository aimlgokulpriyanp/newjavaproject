package com.grievancetrack.scheduler;

import com.grievancetrack.entity.Department;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.service.EscalationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlaEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaEscalationScheduler.class);

    private final GrievanceRepository grievanceRepository;
    private final EscalationService escalationService;

    public SlaEscalationScheduler(GrievanceRepository grievanceRepository, EscalationService escalationService) {
        this.grievanceRepository = grievanceRepository;
        this.escalationService = escalationService;
    }

    /**
     * Periodic SLA compliance checker:
     * Runs every 60 seconds (1 minute) for development/demo.
     * Finds unresolved grievances where current time > slaDeadline and escalates them automatically.
     */
    @Scheduled(fixedRateString = "${sla.escalation.fixed-rate:60000}")
    @Transactional
    public void checkAndEscalateBreachedGrievances() {
        LocalDateTime now = LocalDateTime.now();
        List<Grievance> breachedGrievances = grievanceRepository.findBreachedGrievancesForEscalation(now);

        if (breachedGrievances.isEmpty()) {
            log.debug("SLA Scheduler: No SLA breached grievances detected at {}", now);
            return;
        }

        log.info("SLA Scheduler: Found {} breached grievance(s) requiring automatic escalation at {}",
                breachedGrievances.size(), now);

        for (Grievance grievance : breachedGrievances) {
            try {
                // Determine senior officer name based on department
                Department dept = grievance.getDepartment();
                String seniorOfficer = (dept != null && dept.getOfficerName() != null)
                        ? "Senior Commissioner - " + dept.getName()
                        : "Zonal Senior Grievance Officer";

                String reason = "SLA exceeded without resolution";

                escalationService.escalateGrievance(grievance, seniorOfficer, reason);
                log.info("Successfully escalated grievance {} (Deadline was: {})",
                        grievance.getComplaintCode(), grievance.getSlaDeadline());
            } catch (Exception e) {
                log.error("Failed to automatically escalate grievance id={}: {}", grievance.getId(), e.getMessage(), e);
            }
        }
    }
}
