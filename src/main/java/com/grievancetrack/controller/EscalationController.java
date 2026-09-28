package com.grievancetrack.controller;

import com.grievancetrack.dto.response.EscalationResponse;
import com.grievancetrack.service.EscalationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalations")
@Tag(name = "Escalations", description = "SLA Breach and Senior Officer escalation audit tracking")
public class EscalationController {

    private final EscalationService escalationService;

    public EscalationController(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @GetMapping
    @Operation(summary = "Get all escalation audit records")
    public ResponseEntity<List<EscalationResponse>> getAllEscalations() {
        return ResponseEntity.ok(escalationService.getAllEscalations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get escalation record by ID")
    public ResponseEntity<EscalationResponse> getEscalationById(@PathVariable Long id) {
        return ResponseEntity.ok(escalationService.getEscalationById(id));
    }

    @GetMapping("/grievance/{grievanceId}")
    @Operation(summary = "Get escalation details for a specific grievance")
    public ResponseEntity<EscalationResponse> getEscalationByGrievanceId(@PathVariable Long grievanceId) {
        return ResponseEntity.ok(escalationService.getEscalationByGrievanceId(grievanceId));
    }
}
