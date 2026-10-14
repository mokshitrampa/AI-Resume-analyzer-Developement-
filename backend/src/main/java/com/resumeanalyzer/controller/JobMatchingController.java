package com.resumeanalyzer.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.dto.matching.CustomMatchRequestDto;
import com.resumeanalyzer.dto.matching.JobMatchRequestDto;
import com.resumeanalyzer.dto.matching.JobMatchResponseDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.dto.skillgap.SkillGapResponseDto;
import com.resumeanalyzer.entity.ExtractedResumeData;
import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.JobMatchResult;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedException;
import com.resumeanalyzer.repository.ExtractedResumeDataRepository;
import com.resumeanalyzer.repository.JobMatchResultRepository;
import com.resumeanalyzer.repository.JobRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.JobMatchingEngine;
import com.resumeanalyzer.service.JobService;
import com.resumeanalyzer.service.SkillGapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matching")
@Tag(name = "Job Matching", description = "Endpoints for weighted job matching, custom descriptions, skill gaps, and recommendations")
public class JobMatchingController {

    private final ResumeRepository resumeRepository;
    private final ExtractedResumeDataRepository extractedResumeDataRepository;
    private final JobRepository jobRepository;
    private final JobMatchResultRepository jobMatchResultRepository;
    private final JobMatchingEngine jobMatchingEngine;
    private final SkillGapService skillGapService;
    private final JobService jobService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobMatchingController(ResumeRepository resumeRepository,
                                 ExtractedResumeDataRepository extractedResumeDataRepository,
                                 JobRepository jobRepository,
                                 JobMatchResultRepository jobMatchResultRepository,
                                 JobMatchingEngine jobMatchingEngine,
                                 SkillGapService skillGapService,
                                 JobService jobService) {
        this.resumeRepository = resumeRepository;
        this.extractedResumeDataRepository = extractedResumeDataRepository;
        this.jobRepository = jobRepository;
        this.jobMatchResultRepository = jobMatchResultRepository;
        this.jobMatchingEngine = jobMatchingEngine;
        this.skillGapService = skillGapService;
        this.jobService = jobService;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Match candidate resume against a specific platform job")
    public ResponseEntity<JobMatchResponseDto> analyzeMatch(@Valid @RequestBody JobMatchRequestDto request,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + request.getResumeId()));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(resume.getId())
                .orElseThrow(() -> new BadRequestException("Resume has not been extracted."));

        ExtractedDataDto dataDto = mapToDto(data);
        JobMatchResponseDto result = jobMatchingEngine.matchResumeWithJob(resume, dataDto, job);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/custom")
    @Operation(summary = "Match resume against custom pasted job description text")
    public ResponseEntity<JobMatchResponseDto> analyzeCustomMatch(@Valid @RequestBody CustomMatchRequestDto request,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(resume.getId())
                .orElseThrow(() -> new BadRequestException("Resume has not been extracted."));

        ExtractedDataDto dataDto = mapToDto(data);
        JobMatchResponseDto result = jobMatchingEngine.matchResumeWithCustomText(
                resume, dataDto, request.getJobTitle(), request.getJobDescription(),
                request.getRequiredSkills(), request.getPreferredSkills()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/skill-gap")
    @Operation(summary = "Get customized skill gap analysis and 5-phase learning roadmap")
    public ResponseEntity<SkillGapResponseDto> getSkillGap(@RequestParam Long resumeId,
                                                           @RequestParam(required = false) Long jobId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new BadRequestException("Resume data not found"));

        Job job = null;
        if (jobId != null) {
            job = jobRepository.findById(jobId).orElse(null);
        }

        ExtractedDataDto dataDto = mapToDto(data);
        SkillGapResponseDto gap = skillGapService.analyzeSkillGap(resumeId, dataDto, job);
        return ResponseEntity.ok(gap);
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get database-seeded job recommendations sorted by match score")
    public ResponseEntity<List<JobDto>> getRecommendations(@RequestParam Long resumeId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        List<JobDto> recommended = jobService.getRecommendedJobs(resumeId);
        return ResponseEntity.ok(recommended);
    }

    @GetMapping("/history")
    @Operation(summary = "Get student's match score history")
    public ResponseEntity<List<JobMatchResponseDto>> getMatchingHistory(@AuthenticationPrincipal UserPrincipal principal) {
        List<JobMatchResult> results = jobMatchResultRepository.findByResumeUserIdOrderByMatchedAtDesc(principal.getId());
        List<JobMatchResponseDto> dtos = results.stream().map(r -> {
            JobMatchResponseDto dto = new JobMatchResponseDto();
            dto.setId(r.getId());
            dto.setResumeId(r.getResume().getId());
            dto.setJobId(r.getJob() != null ? r.getJob().getId() : null);
            dto.setJobTitle(r.getJob() != null ? r.getJob().getTitle() : r.getCustomJobTitle());
            dto.setCompanyName(r.getJob() != null ? r.getJob().getCompanyName() : "Custom Matching");
            dto.setOverallScore(r.getOverallScore());
            dto.setRequiredSkillsScore(r.getRequiredSkillsScore());
            dto.setPreferredSkillsScore(r.getPreferredSkillsScore());
            dto.setSemanticScore(r.getSemanticScore());
            dto.setExperienceScore(r.getExperienceScore());
            dto.setEducationScore(r.getEducationScore());
            dto.setExplanation(r.getExplanation());
            dto.setMatchedAt(r.getMatchedAt());

            try {
                if (r.getMatchingSkillsJson() != null)
                    dto.setMatchingSkills(objectMapper.readValue(r.getMatchingSkillsJson(), new TypeReference<>() {}));
                if (r.getMissingSkillsJson() != null)
                    dto.setMissingSkills(objectMapper.readValue(r.getMissingSkillsJson(), new TypeReference<>() {}));
            } catch (Exception ignored) {}

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ExtractedDataDto mapToDto(ExtractedResumeData data) {
        ExtractedDataDto dto = new ExtractedDataDto();
        dto.setId(data.getId());
        dto.setCandidateName(data.getCandidateName());
        dto.setEmail(data.getEmail());
        dto.setPhone(data.getPhone());
        dto.setLocation(data.getLocation());
        dto.setDegree(data.getDegree());
        dto.setGraduationYear(data.getGraduationYear());
        dto.setCgpa(data.getCgpa());
        dto.setRawText(data.getRawText());

        try {
            if (data.getTechnicalSkillsJson() != null)
                dto.setTechnicalSkills(objectMapper.readValue(data.getTechnicalSkillsJson(), new TypeReference<>() {}));
            if (data.getExperienceJson() != null)
                dto.setExperience(objectMapper.readValue(data.getExperienceJson(), new TypeReference<>() {}));
            if (data.getEducationJson() != null)
                dto.setEducation(objectMapper.readValue(data.getEducationJson(), new TypeReference<>() {}));
            if (data.getProjectsJson() != null)
                dto.setProjects(objectMapper.readValue(data.getProjectsJson(), new TypeReference<>() {}));
            if (data.getCertificationsJson() != null)
                dto.setCertifications(objectMapper.readValue(data.getCertificationsJson(), new TypeReference<>() {}));
        } catch (Exception ignored) {}

        return dto;
    }
}
