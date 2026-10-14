package com.resumeanalyzer.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.analysis.AiImprovementDto;
import com.resumeanalyzer.dto.analysis.ResumeAnalysisResponseDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.entity.ExtractedResumeData;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedException;
import com.resumeanalyzer.repository.ExtractedResumeDataRepository;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.ResumeAnalysisService;
import com.resumeanalyzer.service.ResumeImprovementAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "Analysis", description = "Endpoints for resume quality scoring, rubric breakdowns, and AI improvement suggestions")
public class AnalysisController {

    private final ResumeRepository resumeRepository;
    private final ExtractedResumeDataRepository extractedResumeDataRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeImprovementAssistant resumeImprovementAssistant;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisController(ResumeRepository resumeRepository,
                              ExtractedResumeDataRepository extractedResumeDataRepository,
                              ResumeAnalysisRepository resumeAnalysisRepository,
                              ResumeAnalysisService resumeAnalysisService,
                              ResumeImprovementAssistant resumeImprovementAssistant) {
        this.resumeRepository = resumeRepository;
        this.extractedResumeDataRepository = extractedResumeDataRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.resumeAnalysisService = resumeAnalysisService;
        this.resumeImprovementAssistant = resumeImprovementAssistant;
    }

    @PostMapping("/resume/{id}")
    @Operation(summary = "Execute deterministic resume quality analysis")
    public ResponseEntity<ResumeAnalysisResponseDto> analyzeResume(@PathVariable Long id,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(id)
                .orElseThrow(() -> new BadRequestException("Resume has not been parsed yet."));

        ExtractedDataDto dataDto = mapToDto(data);
        ResumeAnalysisResponseDto analysis = resumeAnalysisService.analyzeQuality(resume, dataDto);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific analysis result by ID")
    public ResponseEntity<ResumeAnalysisResponseDto> getAnalysisById(@PathVariable Long id,
                                                                     @AuthenticationPrincipal UserPrincipal principal) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));

        if (!analysis.getResume().getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        return ResponseEntity.ok(toDto(analysis));
    }

    @GetMapping("/history")
    @Operation(summary = "Get user's resume analysis history")
    public ResponseEntity<List<ResumeAnalysisResponseDto>> getAnalysisHistory(@AuthenticationPrincipal UserPrincipal principal) {
        List<ResumeAnalysis> list = resumeAnalysisRepository.findByResumeUserIdOrderByAnalyzedAtDesc(principal.getId());
        return ResponseEntity.ok(list.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/resume/{id}/improvements")
    @Operation(summary = "Get side-by-side action verb and phrasing improvement recommendations")
    public ResponseEntity<List<AiImprovementDto>> getImprovements(@PathVariable Long id,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(id)
                .orElseThrow(() -> new BadRequestException("Resume data not found"));

        ExtractedDataDto dataDto = mapToDto(data);
        List<AiImprovementDto> suggestions = resumeImprovementAssistant.generateImprovements(dataDto);
        return ResponseEntity.ok(suggestions);
    }

    private ResumeAnalysisResponseDto toDto(ResumeAnalysis entity) {
        ResumeAnalysisResponseDto dto = new ResumeAnalysisResponseDto();
        dto.setId(entity.getId());
        dto.setResumeId(entity.getResume().getId());
        dto.setOverallScore(entity.getOverallScore());
        dto.setCompletenessScore(entity.getCompletenessScore());
        dto.setSkillCoverageScore(entity.getSkillCoverageScore());
        dto.setEducationScore(entity.getEducationScore());
        dto.setExperienceScore(entity.getExperienceScore());
        dto.setProjectScore(entity.getProjectScore());
        dto.setFormattingScore(entity.getFormattingScore());
        dto.setKeywordRelevanceScore(entity.getKeywordRelevanceScore());
        dto.setAnalyzedAt(entity.getAnalyzedAt());

        try {
            if (entity.getStrengthsJson() != null)
                dto.setStrengths(objectMapper.readValue(entity.getStrengthsJson(), new TypeReference<>() {}));
            if (entity.getWeaknessesJson() != null)
                dto.setWeaknesses(objectMapper.readValue(entity.getWeaknessesJson(), new TypeReference<>() {}));
            if (entity.getMissingInfoJson() != null)
                dto.setMissingInfo(objectMapper.readValue(entity.getMissingInfoJson(), new TypeReference<>() {}));
            if (entity.getSuggestionsJson() != null)
                dto.setImprovementSuggestions(objectMapper.readValue(entity.getSuggestionsJson(), new TypeReference<>() {}));
        } catch (Exception ignored) {}

        return dto;
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
