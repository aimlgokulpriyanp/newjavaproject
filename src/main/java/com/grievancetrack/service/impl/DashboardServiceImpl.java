package com.grievancetrack.service.impl;

import com.grievancetrack.dto.response.DashboardStatsResponse;
import com.grievancetrack.dto.response.GrievanceResponse;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.SlaStatus;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.repository.RatingRepository;
import com.grievancetrack.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final GrievanceRepository grievanceRepository;
    private final RatingRepository ratingRepository;

    public DashboardServiceImpl(GrievanceRepository grievanceRepository, RatingRepository ratingRepository) {
        this.grievanceRepository = grievanceRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalComplaints(grievanceRepository.count());
        stats.setPending(grievanceRepository.countByStatus(GrievanceStatus.PENDING));
        stats.setAssigned(grievanceRepository.countByStatus(GrievanceStatus.ASSIGNED));
        stats.setInProgress(grievanceRepository.countByStatus(GrievanceStatus.IN_PROGRESS));
        stats.setResolved(grievanceRepository.countByStatus(GrievanceStatus.RESOLVED));
        stats.setEscalated(grievanceRepository.countByStatus(GrievanceStatus.ESCALATED));
        stats.setRejected(grievanceRepository.countByStatus(GrievanceStatus.REJECTED));

        LocalDateTime now = LocalDateTime.now();
        stats.setSlaBreached(grievanceRepository.countSlaBreached(now));

        Double avg = ratingRepository.findAverageRating();
        stats.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        stats.setTotalRatings(ratingRepository.count());

        // Category breakdown
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] row : grievanceRepository.countByCategories()) {
            if (row != null && row.length >= 2) {
                categoryMap.put((String) row[0], ((Number) row[1]).longValue());
            }
        }
        stats.setComplaintsByCategory(categoryMap);

        // Status breakdown
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : grievanceRepository.countByStatuses()) {
            if (row != null && row.length >= 2) {
                statusMap.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        stats.setComplaintsByStatus(statusMap);

        // Escalation by Department
        Map<String, Long> deptEscalationMap = new HashMap<>();
        for (Object[] row : grievanceRepository.countEscalationsByDepartment()) {
            if (row != null && row.length >= 2) {
                deptEscalationMap.put((String) row[0], ((Number) row[1]).longValue());
            }
        }
        stats.setEscalationsByDepartment(deptEscalationMap);

        // SLA status breakdown
        Map<String, Long> slaStatusMap = new HashMap<>();
        for (SlaStatus s : SlaStatus.values()) {
            slaStatusMap.put(s.name(), 0L);
        }
        List<Grievance> allGrievances = grievanceRepository.findAll();
        for (Grievance g : allGrievances) {
            SlaStatus status = g.getSlaStatus();
            slaStatusMap.put(status.name(), slaStatusMap.getOrDefault(status.name(), 0L) + 1);
        }
        stats.setComplaintsBySlaStatus(slaStatusMap);

        // Recent 10 complaints
        stats.setRecentComplaints(
                grievanceRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(GrievanceResponse::fromEntity)
                        .collect(Collectors.toList())
        );

        return stats;
    }
}
