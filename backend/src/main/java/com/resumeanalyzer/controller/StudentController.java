package com.resumeanalyzer.controller;

import com.resumeanalyzer.entity.StudentProfile;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
@Tag(name = "Student", description = "Endpoints for student portal dashboard and profile")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get full student dashboard state: latest resume, match scores, recommended jobs, stats")
    public ResponseEntity<Map<String, Object>> getDashboardData(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getDashboardData(principal.getId()));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get student educational and career profile")
    public ResponseEntity<StudentProfile> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getProfile(principal.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update student educational and career profile")
    public ResponseEntity<StudentProfile> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestBody StudentProfile profile) {
        return ResponseEntity.ok(studentService.updateProfile(principal.getId(), profile));
    }
}
