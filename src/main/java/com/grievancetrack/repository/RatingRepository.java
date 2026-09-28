package com.grievancetrack.repository;

import com.grievancetrack.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByGrievanceId(Long grievanceId);
    boolean existsByGrievanceId(Long grievanceId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Rating r")
    Double findAverageRating();
}
