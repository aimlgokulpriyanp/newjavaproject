package com.grievancetrack;

import com.grievancetrack.entity.Category;
import com.grievancetrack.entity.Department;
import com.grievancetrack.entity.Escalation;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.SlaStatus;
import com.grievancetrack.repository.CategoryRepository;
import com.grievancetrack.repository.DepartmentRepository;
import com.grievancetrack.repository.EscalationRepository;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.scheduler.SlaEscalationScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SlaEscalationSchedulerTest {

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EscalationRepository escalationRepository;

    @Autowired
    private SlaEscalationScheduler slaEscalationScheduler;

    @Test
    @Transactional
    @DisplayName("Test 8, 9, 10, 11, 12: SLA Breach, Auto Escalation, Officer & Timestamp Recorded, Duplicate Prevention")
    void testAutomaticSlaEscalation() {
        Department dept = departmentRepository.findAll().get(0);
        Category cat = categoryRepository.findAll().get(0);

        // Create an overdue unresolved grievance with past SLA deadline
        LocalDateTime pastCreated = LocalDateTime.now().minusDays(10);
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(7); // Deadline passed 7 days ago!

        Grievance overdueGrievance = new Grievance(
                "GRV-TEST-SLA",
                "Escalation Citizen",
                "esc@test.com",
                "9876599999",
                "Severe sewage leak overflowing continuously for a week",
                "Lane 99, Civic Block",
                cat,
                dept,
                pastCreated,
                pastDeadline
        );
        overdueGrievance.setStatus(GrievanceStatus.IN_PROGRESS);
        Grievance saved = grievanceRepository.save(overdueGrievance);

        // Verify SLA status is SLA_BREACHED prior to scheduler execution
        assertEquals(SlaStatus.SLA_BREACHED, saved.getSlaStatus(), "Expected SLA_BREACHED before escalation");

        // Trigger scheduler
        slaEscalationScheduler.checkAndEscalateBreachedGrievances();

        // Refresh from DB
        Grievance escalatedGrievance = grievanceRepository.findById(saved.getId()).orElseThrow();

        // 9. Status automatically changed to ESCALATED
        assertEquals(GrievanceStatus.ESCALATED, escalatedGrievance.getStatus(), "Status must be updated to ESCALATED");
        assertEquals(SlaStatus.ESCALATED, escalatedGrievance.getSlaStatus());

        // 6. Escalation record created
        Escalation escalation = escalationRepository.findByGrievanceId(saved.getId()).orElse(null);
        assertNotNull(escalation, "Escalation record must exist");

        // 11. Escalation officer recorded
        assertNotNull(escalation.getSeniorOfficerName(), "Senior officer name must be recorded");
        assertTrue(escalation.getSeniorOfficerName().length() > 0);

        // 12. Escalation timestamp recorded
        assertNotNull(escalation.getEscalatedAt(), "Escalation timestamp must be recorded");

        // Escalation reason recorded
        assertEquals("SLA exceeded without resolution", escalation.getReason());

        // 10. Duplicate escalation prevention: running scheduler again should NOT create another escalation record
        long escalationCountBefore = escalationRepository.count();
        slaEscalationScheduler.checkAndEscalateBreachedGrievances();
        long escalationCountAfter = escalationRepository.count();

        assertEquals(escalationCountBefore, escalationCountAfter, "Duplicate escalation record must NOT be created");
    }
}
