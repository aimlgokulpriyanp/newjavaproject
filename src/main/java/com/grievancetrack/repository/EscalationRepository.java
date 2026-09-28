package com.grievancetrack.repository;

import com.grievancetrack.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    Optional<Escalation> findByGrievanceId(Long grievanceId);
    boolean existsByGrievanceId(Long grievanceId);
    List<Escalation> findAllByOrderByEscalatedAtDesc();
}
