package com.grievancetrack.controller;

import com.grievancetrack.dto.request.RatingRequest;
import com.grievancetrack.dto.response.RatingResponse;
import com.grievancetrack.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grievances/{grievanceId}/rating")
@Tag(name = "Ratings", description = "Citizen satisfaction ratings and feedback on resolved grievances")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @Operation(summary = "Submit a 1-5 star citizen rating for a resolved grievance")
    public ResponseEntity<RatingResponse> submitRating(
            @PathVariable Long grievanceId,
            @Valid @RequestBody RatingRequest request
    ) {
        RatingResponse response = ratingService.submitRating(grievanceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get the citizen rating for a specific grievance")
    public ResponseEntity<RatingResponse> getRating(@PathVariable Long grievanceId) {
        return ResponseEntity.ok(ratingService.getRatingByGrievanceId(grievanceId));
    }
}
