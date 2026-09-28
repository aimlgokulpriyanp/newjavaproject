package com.grievancetrack.controller;

import com.grievancetrack.dto.request.GrievanceCreateRequest;
import com.grievancetrack.dto.request.GrievanceStatusUpdateRequest;
import com.grievancetrack.dto.response.GrievanceResponse;
import com.grievancetrack.dto.response.PageResponse;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.service.GrievanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grievances")
@Tag(name = "Grievances", description = "Citizen grievance registration, tracking, pagination, sorting, and status transitions")
public class GrievanceController {

    private final GrievanceService grievanceService;

    public GrievanceController(GrievanceService grievanceService) {
        this.grievanceService = grievanceService;
    }

    @PostMapping
    @Operation(summary = "Submit a new citizen grievance (Auto-routes to department, calculates SLA deadline, assigns GRV code)")
    public ResponseEntity<GrievanceResponse> createGrievance(@Valid @RequestBody GrievanceCreateRequest request) {
        GrievanceResponse created = grievanceService.createGrievance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get paginated, sorted, and filtered grievances")
    public ResponseEntity<PageResponse<GrievanceResponse>> getGrievances(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort expression (e.g. createdAt,desc or slaDeadline,asc)") @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Keyword search (code, citizen, description, location)") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status") @RequestParam(required = false) GrievanceStatus status,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by department ID") @RequestParam(required = false) Long departmentId
    ) {
        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();
        Sort.Direction direction = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Map frontend sort names to entity property names safely
        if ("category".equalsIgnoreCase(property)) {
            property = "category.name";
        } else if ("department".equalsIgnoreCase(property)) {
            property = "department.name";
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(direction, property));
        return ResponseEntity.ok(grievanceService.getGrievances(search, status, categoryId, departmentId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grievance by internal ID")
    public ResponseEntity<GrievanceResponse> getGrievanceById(@PathVariable Long id) {
        return ResponseEntity.ok(grievanceService.getGrievanceById(id));
    }

    @GetMapping("/code/{complaintCode}")
    @Operation(summary = "Track grievance by unique complaint code (e.g. GRV-000001)")
    public ResponseEntity<GrievanceResponse> getGrievanceByCode(@PathVariable String complaintCode) {
        return ResponseEntity.ok(grievanceService.getGrievanceByCode(complaintCode));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update grievance status (Enforces valid lifecycle transitions)")
    public ResponseEntity<GrievanceResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody GrievanceStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(grievanceService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete grievance by ID")
    public ResponseEntity<Void> deleteGrievance(@PathVariable Long id) {
        grievanceService.deleteGrievance(id);
        return ResponseEntity.noContent().build();
    }
}
