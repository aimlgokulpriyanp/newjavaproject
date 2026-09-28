package com.grievancetrack.dto.response;

import com.grievancetrack.entity.Rating;
import java.time.LocalDateTime;

public class RatingResponse {
    private Long id;
    private Long grievanceId;
    private String complaintCode;
    private Integer rating;
    private String feedback;
    private LocalDateTime ratedAt;

    public RatingResponse() {
    }

    public static RatingResponse fromEntity(Rating rating) {
        if (rating == null) return null;
        RatingResponse dto = new RatingResponse();
        dto.setId(rating.getId());
        if (rating.getGrievance() != null) {
            dto.setGrievanceId(rating.getGrievance().getId());
            dto.setComplaintCode(rating.getGrievance().getComplaintCode());
        }
        dto.setRating(rating.getRating());
        dto.setFeedback(rating.getFeedback());
        dto.setRatedAt(rating.getRatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(Long grievanceId) {
        this.grievanceId = grievanceId;
    }

    public String getComplaintCode() {
        return complaintCode;
    }

    public void setComplaintCode(String complaintCode) {
        this.complaintCode = complaintCode;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getRatedAt() {
        return ratedAt;
    }

    public void setRatedAt(LocalDateTime ratedAt) {
        this.ratedAt = ratedAt;
    }
}
