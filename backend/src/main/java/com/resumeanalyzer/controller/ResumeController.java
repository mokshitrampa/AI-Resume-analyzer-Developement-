package com.resumeanalyzer.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.dto.resume.ExtractedUpdateDto;
import com.resumeanalyzer.dto.resume.ResumeResponseDto;
import com.resumeanalyzer.entity.ExtractedResumeData;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.StudentProfile;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.FileStorageService;
import com.resumeanalyzer.service.ResumeParserService;
import com.resumeanalyzer.service.SkillExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resumes", description = "Endpoints for resume upload, parsing, extraction, and management")
public class ResumeController {

    private final FileStorageService fileStorageService;
    private final ResumeParserService resumeParserService;
    private final SkillExtractionService skillExtractionService;
    private final ResumeRepository resumeRepository;
    private final ExtractedResumeDataRepository extractedResumeDataRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeController(FileStorageService fileStorageService,
                            ResumeParserService resumeParserService,
                            SkillExtractionService skillExtractionService,
                            ResumeRepository resumeRepository,
                            ExtractedResumeDataRepository extractedResumeDataRepository,
                            StudentProfileRepository studentProfileRepository,
                            ResumeAnalysisRepository resumeAnalysisRepository) {
        this.fileStorageService = fileStorageService;
        this.resumeParserService = resumeParserService;
        this.skillExtractionService = skillExtractionService;
        this.resumeRepository = resumeRepository;
        this.extractedResumeDataRepository = extractedResumeDataRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload PDF or DOCX resume, parse text, and extract skills")
    public ResponseEntity<ExtractedDataDto> uploadResume(@RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");

        String originalFilename = file.getOriginalFilename();
        String storedFilename = fileStorageService.storeFile(file);
        Path filePath = fileStorageService.loadFileAsPath(storedFilename);

        String ext = storedFilename.substring(storedFilename.lastIndexOf('.') + 1);

        // 1. Save Resume Entity
        User user = new User();
        user.setId(principal.getId());

        int version = (int) resumeRepository.countByUser(user) + 1;

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setOriginalFilename(originalFilename);
        resume.setStoredFilename(storedFilename);
        resume.setFileType(ext);
        resume.setFileSize(file.getSize());
        resume.setFilePath(filePath.toString());
        resume.setVersion(version);

        Resume savedResume = resumeRepository.save(resume);

        // 2. Extract plain structured text with Apache PDFBox / POI
        String rawText = resumeParserService.extractRawText(new File(filePath.toString()), ext);

        // 3. Heuristic detail parsing
        ExtractedDataDto extractedDto = resumeParserService.parseResumeDetails(rawText);
        extractedDto.setResumeId(savedResume.getId());

        // 4. NLP Skill Extraction with synonyms and alias dictionary
        List<String> skills = skillExtractionService.extractSkills(rawText);
        extractedDto.setTechnicalSkills(skills);

        // 5. Save ExtractedResumeData Entity
        ExtractedResumeData dataEntity = new ExtractedResumeData(savedResume);
        dataEntity.setCandidateName(extractedDto.getCandidateName());
        dataEntity.setEmail(extractedDto.getEmail());
        dataEntity.setPhone(extractedDto.getPhone());
        dataEntity.setLocation(extractedDto.getLocation());
        dataEntity.setDegree(extractedDto.getDegree());
        dataEntity.setGraduationYear(extractedDto.getGraduationYear());
        dataEntity.setCgpa(extractedDto.getCgpa());
        dataEntity.setRawText(rawText);

        try {
            dataEntity.setTechnicalSkillsJson(objectMapper.writeValueAsString(skills));
            dataEntity.setExperienceJson(objectMapper.writeValueAsString(extractedDto.getExperience()));
            dataEntity.setEducationJson(objectMapper.writeValueAsString(extractedDto.getEducation()));
            dataEntity.setProjectsJson(objectMapper.writeValueAsString(extractedDto.getProjects()));
            dataEntity.setCertificationsJson(objectMapper.writeValueAsString(extractedDto.getCertifications()));
        } catch (Exception ignored) {}

        ExtractedResumeData savedData = extractedResumeDataRepository.save(dataEntity);
        extractedDto.setId(savedData.getId());
        extractedDto.setExtractedAt(savedData.getExtractedAt());

        // Update student profile count
        studentProfileRepository.findByUserId(principal.getId()).ifPresent(p -> {
            p.setResumeCount(p.getResumeCount() + 1);
            studentProfileRepository.save(p);
        });

        return ResponseEntity.ok(extractedDto);
    }

    @GetMapping
    @Operation(summary = "Get list of all uploaded resumes for authenticated user")
    public ResponseEntity<List<ResumeResponseDto>> getAllUserResumes(@AuthenticationPrincipal UserPrincipal principal) {
        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(principal.getId());
        List<ResumeResponseDto> list = resumes.stream().map(r -> {
            ResumeResponseDto dto = new ResumeResponseDto(
                    r.getId(), r.getOriginalFilename(), r.getFileType(), r.getFileSize(), r.getVersion(), r.getCreatedAt()
            );
            resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(r.getId()).ifPresent(a -> {
                dto.setQualityScore(a.getOverallScore());
            });
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resume metadata by ID")
    public ResponseEntity<ResumeResponseDto> getResumeById(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied to requested resume");
        }

        ResumeResponseDto dto = new ResumeResponseDto(
                resume.getId(), resume.getOriginalFilename(), resume.getFileType(), resume.getFileSize(), resume.getVersion(), resume.getCreatedAt()
        );
        resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(resume.getId()).ifPresent(a -> {
            dto.setQualityScore(a.getOverallScore());
        });

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete resume and stored physical document")
    public ResponseEntity<Map<String, String>> deleteResume(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Cannot delete someone else's resume");
        }

        // Delete physical file
        try {
            fileStorageService.deleteFile(resume.getStoredFilename());
        } catch (Exception ignored) {}

        resumeRepository.delete(resume);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }

    @GetMapping("/{id}/extracted-data")
    @Operation(summary = "Get extracted resume details")
    public ResponseEntity<ExtractedDataDto> getExtractedData(@PathVariable Long id,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extracted data not found for resume ID: " + id));

        ExtractedDataDto dto = mapDataToDto(data);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/extracted-data")
    @Operation(summary = "Allow user to review and correct extracted details before saving analysis")
    public ResponseEntity<ExtractedDataDto> updateExtractedData(@PathVariable Long id,
                                                                @RequestBody ExtractedUpdateDto updateDto,
                                                                @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Access denied");
        }

        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extracted data not found"));

        if (updateDto.getCandidateName() != null) data.setCandidateName(updateDto.getCandidateName());
        if (updateDto.getEmail() != null) data.setEmail(updateDto.getEmail());
        if (updateDto.getPhone() != null) data.setPhone(updateDto.getPhone());
        if (updateDto.getLocation() != null) data.setLocation(updateDto.getLocation());
        if (updateDto.getDegree() != null) data.setDegree(updateDto.getDegree());
        if (updateDto.getInstitution() != null) data.setInstitution(updateDto.getInstitution());
        if (updateDto.getGraduationYear() != null) data.setGraduationYear(updateDto.getGraduationYear());
        if (updateDto.getCgpa() != null) data.setCgpa(updateDto.getCgpa());

        if (updateDto.getTechnicalSkills() != null) {
            try {
                data.setTechnicalSkillsJson(objectMapper.writeValueAsString(updateDto.getTechnicalSkills()));
            } catch (Exception ignored) {}
        }

        ExtractedResumeData saved = extractedResumeDataRepository.save(data);
        return ResponseEntity.ok(mapDataToDto(saved));
    }

    private ExtractedDataDto mapDataToDto(ExtractedResumeData data) {
        ExtractedDataDto dto = new ExtractedDataDto();
        dto.setId(data.getId());
        dto.setResumeId(data.getResume().getId());
        dto.setCandidateName(data.getCandidateName());
        dto.setEmail(data.getEmail());
        dto.setPhone(data.getPhone());
        dto.setLocation(data.getLocation());
        dto.setDegree(data.getDegree());
        dto.setInstitution(data.getInstitution());
        dto.setGraduationYear(data.getGraduationYear());
        dto.setCgpa(data.getCgpa());
        dto.setRawText(data.getRawText());
        dto.setExtractedAt(data.getExtractedAt());

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
