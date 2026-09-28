package com.grievancetrack.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStatsResponse {
    private long totalComplaints;
    private long pending;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long escalated;
    private long rejected;
    private long slaBreached;
    private double averageRating;
    private long totalRatings;

    private Map<String, Long> complaintsByCategory = new HashMap<>();
    private Map<String, Long> complaintsByStatus = new HashMap<>();
    private Map<String, Long> escalationsByDepartment = new HashMap<>();
    private Map<String, Long> complaintsBySlaStatus = new HashMap<>();

    private List<GrievanceResponse> recentComplaints;

    public DashboardStatsResponse() {
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getAssigned() {
        return assigned;
    }

    public void setAssigned(long assigned) {
        this.assigned = assigned;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getEscalated() {
        return escalated;
    }

    public void setEscalated(long escalated) {
        this.escalated = escalated;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(long slaBreached) {
        this.slaBreached = slaBreached;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public Map<String, Long> getComplaintsByCategory() {
        return complaintsByCategory;
    }

    public void setComplaintsByCategory(Map<String, Long> complaintsByCategory) {
        this.complaintsByCategory = complaintsByCategory;
    }

    public Map<String, Long> getComplaintsByStatus() {
        return complaintsByStatus;
    }

    public void setComplaintsByStatus(Map<String, Long> complaintsByStatus) {
        this.complaintsByStatus = complaintsByStatus;
    }

    public Map<String, Long> getEscalationsByDepartment() {
        return escalationsByDepartment;
    }

    public void setEscalationsByDepartment(Map<String, Long> escalationsByDepartment) {
        this.escalationsByDepartment = escalationsByDepartment;
    }

    public Map<String, Long> getComplaintsBySlaStatus() {
        return complaintsBySlaStatus;
    }

    public void setComplaintsBySlaStatus(Map<String, Long> complaintsBySlaStatus) {
        this.complaintsBySlaStatus = complaintsBySlaStatus;
    }

    public List<GrievanceResponse> getRecentComplaints() {
        return recentComplaints;
    }

    public void setRecentComplaints(List<GrievanceResponse> recentComplaints) {
        this.recentComplaints = recentComplaints;
    }
}
