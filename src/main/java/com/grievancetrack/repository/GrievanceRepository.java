package com.grievancetrack.repository;

import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.GrievanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    Optional<Grievance> findByComplaintCodeIgnoreCase(String complaintCode);

    Optional<Grievance> findTopByOrderByIdDesc();

    long countByStatus(GrievanceStatus status);

    long countByCategoryId(Long categoryId);

    long countByDepartmentId(Long departmentId);

    List<Grievance> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT g FROM Grievance g WHERE " +
            "g.status NOT IN (com.grievancetrack.entity.enums.GrievanceStatus.RESOLVED, " +
            "com.grievancetrack.entity.enums.GrievanceStatus.REJECTED, " +
            "com.grievancetrack.entity.enums.GrievanceStatus.ESCALATED) " +
            "AND g.slaDeadline < :now " +
            "AND g.escalation IS NULL")
    List<Grievance> findBreachedGrievancesForEscalation(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE " +
            "g.status NOT IN (com.grievancetrack.entity.enums.GrievanceStatus.RESOLVED, " +
            "com.grievancetrack.entity.enums.GrievanceStatus.REJECTED) " +
            "AND g.slaDeadline < :now")
    long countSlaBreached(@Param("now") LocalDateTime now);

    @Query("SELECT g FROM Grievance g WHERE " +
            "(:status IS NULL OR g.status = :status) AND " +
            "(:categoryId IS NULL OR g.category.id = :categoryId) AND " +
            "(:departmentId IS NULL OR g.department.id = :departmentId) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(g.complaintCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.citizenName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Grievance> findWithFilters(
            @Param("search") String search,
            @Param("status") GrievanceStatus status,
            @Param("categoryId") Long categoryId,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );

    @Query("SELECT g.category.name, COUNT(g) FROM Grievance g GROUP BY g.category.name")
    List<Object[]> countByCategories();

    @Query("SELECT g.status, COUNT(g) FROM Grievance g GROUP BY g.status")
    List<Object[]> countByStatuses();

    @Query("SELECT e.grievance.department.name, COUNT(e) FROM Escalation e GROUP BY e.grievance.department.name")
    List<Object[]> countEscalationsByDepartment();
}
