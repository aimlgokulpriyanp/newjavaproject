package com.grievancetrack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grievancetrack.dto.request.GrievanceCreateRequest;
import com.grievancetrack.dto.request.GrievanceStatusUpdateRequest;
import com.grievancetrack.dto.request.RatingRequest;
import com.grievancetrack.entity.enums.GrievanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GrievanceTrackApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Context loads successfully and seed categories are present")
    void contextLoads() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("Test 1, 4, 5: Create valid grievance, auto-assign department, calculate SLA")
    void testCreateValidGrievance() throws Exception {
        GrievanceCreateRequest request = new GrievanceCreateRequest(
                "Rohit Sharma",
                "rohit.s@example.com",
                "9876500001",
                1L, // Road category (3 days SLA)
                "Broken curbstone and waterlogging on 100ft road",
                "100ft Road, Sector 3"
        );

        mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintCode", startsWith("GRV-")))
                .andExpect(jsonPath("$.citizenName", is("Rohit Sharma")))
                .andExpect(jsonPath("$.department.name", is("Roads Department")))
                .andExpect(jsonPath("$.slaDays", is(3)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.slaDeadline", notNullValue()));
    }

    @Test
    @DisplayName("Test 2: Create invalid grievance returns 400 Bad Request")
    void testCreateInvalidGrievance() throws Exception {
        GrievanceCreateRequest invalidRequest = new GrievanceCreateRequest(
                "", // empty name
                "invalid-email", // invalid email
                "",
                1L,
                "short", // less than 10 chars
                ""
        );

        mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    @DisplayName("Test 3: Category not found returns 404 Not Found")
    void testCategoryNotFound() throws Exception {
        GrievanceCreateRequest request = new GrievanceCreateRequest(
                "Aman Varma",
                "aman.v@example.com",
                "9876500002",
                9999L, // non-existent category
                "Test valid description for non existent category",
                "Some location"
        );

        mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Test 6 & 7: Status transition validation (Valid and Invalid)")
    void testStatusTransitions() throws Exception {
        // Create grievance to test transition
        GrievanceCreateRequest createReq = new GrievanceCreateRequest(
                "Test Citizen", "citizen@test.com", "9876500003", 2L,
                "Valid water issue description for status transition test", "Location A"
        );

        String createRes = mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createRes).get("id").asLong();

        // Invalid: PENDING -> RESOLVED directly (must be rejected)
        GrievanceStatusUpdateRequest invalidTransition = new GrievanceStatusUpdateRequest(
                GrievanceStatus.RESOLVED, "Trying to jump directly to resolved", "Officer"
        );

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransition)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid status transition")));

        // Valid: PENDING -> ASSIGNED
        GrievanceStatusUpdateRequest valid1 = new GrievanceStatusUpdateRequest(
                GrievanceStatus.ASSIGNED, "Assigned to Ward Engineer", "Officer"
        );

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")));

        // Valid: ASSIGNED -> IN_PROGRESS
        GrievanceStatusUpdateRequest valid2 = new GrievanceStatusUpdateRequest(
                GrievanceStatus.IN_PROGRESS, "Crew dispatched", "Officer"
        );

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // Valid: IN_PROGRESS -> RESOLVED
        GrievanceStatusUpdateRequest valid3 = new GrievanceStatusUpdateRequest(
                GrievanceStatus.RESOLVED, "Pipeline repaired successfully", "Officer"
        );

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedAt", notNullValue()));
    }

    @Test
    @DisplayName("Test 13, 14, 15, 16: Citizen Rating rules and edge cases")
    void testCitizenRatings() throws Exception {
        // Create an unresolved grievance (PENDING)
        GrievanceCreateRequest req = new GrievanceCreateRequest(
                "Rating Test Citizen", "rate@test.com", "9876500004", 1L,
                "Description for rating test unresolved check", "Test Location"
        );

        String res = mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(res).get("id").asLong();

        // 14. Rating before RESOLVED must be rejected
        RatingRequest r1 = new RatingRequest(5, "Should fail before resolution");
        mockMvc.perform(post("/api/grievances/" + id + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("only allowed after the grievance has been RESOLVED")));

        // Transition: PENDING -> ASSIGNED -> IN_PROGRESS -> RESOLVED
        mockMvc.perform(put("/api/grievances/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GrievanceStatusUpdateRequest(GrievanceStatus.ASSIGNED, "", ""))));

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GrievanceStatusUpdateRequest(GrievanceStatus.IN_PROGRESS, "", ""))));

        mockMvc.perform(put("/api/grievances/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GrievanceStatusUpdateRequest(GrievanceStatus.RESOLVED, "Fixed", ""))));

        // 16. Rating outside 1-5 rejected (e.g. 6 or 0)
        RatingRequest rInvalid = new RatingRequest(6, "Invalid score");
        mockMvc.perform(post("/api/grievances/" + id + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rInvalid)))
                .andExpect(status().isBadRequest());

        // 13. Valid rating after resolution
        RatingRequest rValid = new RatingRequest(5, "Great civic service!");
        mockMvc.perform(post("/api/grievances/" + id + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rValid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.feedback", is("Great civic service!")));

        // 15. Duplicate rating must be rejected (409 CONFLICT)
        RatingRequest rDuplicate = new RatingRequest(4, "Second rating attempt");
        mockMvc.perform(post("/api/grievances/" + id + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rDuplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already been submitted")));
    }

    @Test
    @DisplayName("Test 17, 18, 19, 20: Pagination, sorting, search, and filtering")
    void testPaginationAndFiltering() throws Exception {
        // Query with pagination and sorting
        mockMvc.perform(get("/api/grievances")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(3)))
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));

        // Create a specific grievance for search testing
        GrievanceCreateRequest searchReq = new GrievanceCreateRequest(
                "SearchableCitizen", "search@test.com", "9876500099", 1L,
                "Unique problem description for search filter verification", "Search Location"
        );
        mockMvc.perform(post("/api/grievances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchReq)))
                .andExpect(status().isCreated());

        // Query with search
        mockMvc.perform(get("/api/grievances")
                        .param("search", "SearchableCitizen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].citizenName", containsString("SearchableCitizen")));

        // Query with status filter
        mockMvc.perform(get("/api/grievances")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status", is("RESOLVED")));
    }

    @Test
    @DisplayName("Test Dashboard Stats API")
    void testDashboardStats() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComplaints", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.complaintsByCategory", notNullValue()))
                .andExpect(jsonPath("$.complaintsByStatus", notNullValue()));
    }
}
