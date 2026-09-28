package com.grievancetrack.service.impl;

import com.grievancetrack.dto.request.RatingRequest;
import com.grievancetrack.dto.response.RatingResponse;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.Rating;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.NotificationType;
import com.grievancetrack.exception.DuplicateRatingException;
import com.grievancetrack.exception.InvalidRequestException;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.GrievanceRepository;
import com.grievancetrack.repository.RatingRepository;
import com.grievancetrack.service.NotificationService;
import com.grievancetrack.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final GrievanceRepository grievanceRepository;
    private final NotificationService notificationService;

    public RatingServiceImpl(RatingRepository ratingRepository,
                             GrievanceRepository grievanceRepository,
                             NotificationService notificationService) {
        this.ratingRepository = ratingRepository;
        this.grievanceRepository = grievanceRepository;
        this.notificationService = notificationService;
    }

    @Override
    public RatingResponse submitRating(Long grievanceId, RatingRequest request) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found with ID: " + grievanceId));

        // Rule 1: Rating allowed ONLY after RESOLVED
        if (grievance.getStatus() != GrievanceStatus.RESOLVED) {
            throw new InvalidRequestException("Rating is only allowed after the grievance has been RESOLVED. Current status: " + grievance.getStatus());
        }

        // Rule 2 & 3: One grievance can have only one rating. Duplicate rating must be rejected
        if (ratingRepository.existsByGrievanceId(grievanceId) || grievance.getRating() != null) {
            throw new DuplicateRatingException("A rating has already been submitted for grievance: " + grievance.getComplaintCode());
        }

        // Rule 4: Rating must be between 1 and 5
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidRequestException("Rating score must be an integer between 1 and 5");
        }

        Rating rating = new Rating(grievance, request.getRating(), request.getFeedback(), LocalDateTime.now());
        Rating saved = ratingRepository.save(rating);

        grievance.setRating(saved);
        grievanceRepository.save(grievance);

        String message = String.format("Citizen submitted a %d-star rating for resolved grievance %s",
                request.getRating(), grievance.getComplaintCode());
        notificationService.createNotification(grievance, message, NotificationType.RATING_SUBMITTED);

        return RatingResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getRatingByGrievanceId(Long grievanceId) {
        Rating rating = ratingRepository.findByGrievanceId(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("No rating found for grievance ID: " + grievanceId));
        return RatingResponse.fromEntity(rating);
    }
}
