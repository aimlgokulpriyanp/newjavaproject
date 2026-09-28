package com.grievancetrack.service;

import com.grievancetrack.dto.request.RatingRequest;
import com.grievancetrack.dto.response.RatingResponse;

public interface RatingService {
    RatingResponse submitRating(Long grievanceId, RatingRequest request);
    RatingResponse getRatingByGrievanceId(Long grievanceId);
}
