package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.admin.AdminStatsDto;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.entity.Skill;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.service.AdminService;
import com.resumeanalyzer.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints for platform administration, user management, and skill dictionary")
public class AdminController {

    private final AdminService adminService;
    private final JobService jobService;

    public AdminController(AdminService adminService, JobService jobService) {
        this.adminService = adminService;
        this.jobService = jobService;
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get system-wide platform metrics and analytics")
    public ResponseEntity<AdminStatsDto> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }

    @GetMapping("/users")
    @Operation(summary = "Paginated list of platform users")
    public ResponseEntity<Page<User>> getUsers(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(page, size, search));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Enable or disable user access")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@PathVariable Long id,
                                                                @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        adminService.toggleUserStatus(id, active);
        return ResponseEntity.ok(Map.of("message", "User account status updated."));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user account")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User account deleted."));
    }

    @GetMapping("/jobs")
    @Operation(summary = "List all platform jobs for moderation")
    public ResponseEntity<List<JobDto>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs(null, null, null));
    }

    @GetMapping("/skills")
    @Operation(summary = "List all skills in the dictionary")
    public ResponseEntity<List<Skill>> getSkills() {
        return ResponseEntity.ok(adminService.getAllSkills());
    }

    @PostMapping("/skills")
    @Operation(summary = "Add a new skill and aliases to the dictionary")
    public ResponseEntity<Skill> addSkill(@RequestBody Skill skill) {
        return ResponseEntity.ok(adminService.addSkill(skill));
    }

    @DeleteMapping("/skills/{id}")
    @Operation(summary = "Delete skill from dictionary")
    public ResponseEntity<Map<String, String>> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return ResponseEntity.ok(Map.of("message", "Skill removed from dictionary."));
    }
}
