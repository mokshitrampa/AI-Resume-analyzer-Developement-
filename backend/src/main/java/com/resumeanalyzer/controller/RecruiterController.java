package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.job.JobCreateRequest;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.dto.recruiter.ApplicationDto;
import com.resumeanalyzer.dto.recruiter.RecruiterStatsDto;
import com.resumeanalyzer.entity.ApplicationStatus;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.RecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruiter")
@Tag(name = "Recruiter", description = "Endpoints for recruiters to post jobs, review candidate applications, and shortlist")
public class RecruiterController {

    private final RecruiterService recruiterService;

    public RecruiterController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get recruiter dashboard metrics")
    public ResponseEntity<RecruiterStatsDto> getDashboardStats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recruiterService.getDashboardStats(principal.getId()));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get jobs posted by current recruiter")
    public ResponseEntity<List<JobDto>> getMyJobs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recruiterService.getMyJobs(principal.getId()));
    }

    @PostMapping("/jobs")
    @Operation(summary = "Publish a new job opening")
    public ResponseEntity<JobDto> createJob(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody JobCreateRequest request) {
        return ResponseEntity.ok(recruiterService.createJob(principal.getId(), request));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Update an existing job opening")
    public ResponseEntity<JobDto> updateJob(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id,
                                            @Valid @RequestBody JobCreateRequest request) {
        return ResponseEntity.ok(recruiterService.updateJob(principal.getId(), id, request));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Delete a job opening")
    public ResponseEntity<Map<String, String>> deleteJob(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable Long id) {
        recruiterService.deleteJob(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Job opening deleted successfully."));
    }

    @GetMapping("/jobs/{id}/applications")
    @Operation(summary = "Get candidate applications for a specific job")
    public ResponseEntity<List<ApplicationDto>> getApplications(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(recruiterService.getJobApplications(principal.getId(), id));
    }

    @PutMapping("/applications/{id}/status")
    @Operation(summary = "Shortlist, review, or reject a candidate application")
    public ResponseEntity<ApplicationDto> updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        String notes = body.get("notes");
        ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(recruiterService.updateApplicationStatus(principal.getId(), id, status, notes));
    }
}
