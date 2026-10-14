package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Endpoints for job browsing, filtering, saving, and applications")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Browse and search all active jobs")
    public ResponseEntity<List<JobDto>> getAllJobs(@RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String location,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(jobService.getAllJobs(title, location, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single job details by ID")
    public ResponseEntity<JobDto> getJobById(@PathVariable Long id,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(jobService.getJobById(id, userId));
    }

    @PostMapping("/{id}/save")
    @Operation(summary = "Bookmark a job for current user")
    public ResponseEntity<Map<String, String>> saveJob(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        jobService.saveJob(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Job saved to your bookmarks."));
    }

    @DeleteMapping("/{id}/save")
    @Operation(summary = "Remove bookmark for a job")
    public ResponseEntity<Map<String, String>> unsaveJob(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        jobService.unsaveJob(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Job removed from bookmarks."));
    }

    @GetMapping("/saved")
    @Operation(summary = "Get all saved jobs for current student")
    public ResponseEntity<List<JobDto>> getSavedJobs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(jobService.getSavedJobs(principal.getId()));
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "Apply for a platform job with resume")
    public ResponseEntity<Map<String, String>> applyForJob(@PathVariable Long id,
                                                           @RequestParam(required = false) Long resumeId,
                                                           @RequestParam(required = false) String coverLetter,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        jobService.applyForJob(principal.getId(), id, resumeId, coverLetter);
        return ResponseEntity.ok(Map.of("message", "Application submitted successfully!"));
    }
}
