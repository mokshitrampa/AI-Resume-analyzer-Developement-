package com.resumeanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.analysis.ResumeAnalysisResponseDto;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.dto.matching.JobMatchResponseDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.dto.resume.ResumeResponseDto;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ResumeRepository resumeRepository;
    private final ExtractedResumeDataRepository extractedResumeDataRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobMatchResultRepository jobMatchResultRepository;
    private final JobService jobService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudentService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          ResumeRepository resumeRepository,
                          ExtractedResumeDataRepository extractedResumeDataRepository,
                          ResumeAnalysisRepository resumeAnalysisRepository,
                          JobMatchResultRepository jobMatchResultRepository,
                          JobService jobService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.resumeRepository = resumeRepository;
        this.extractedResumeDataRepository = extractedResumeDataRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobMatchResultRepository = jobMatchResultRepository;
        this.jobService = jobService;
    }

    public Map<String, Object> getDashboardData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(new StudentProfile(user)));

        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Resume latestResume = resumes.isEmpty() ? null : resumes.get(0);

        ResumeAnalysis latestAnalysis = null;
        ExtractedResumeData extractedData = null;
        if (latestResume != null) {
            latestAnalysis = resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(latestResume.getId()).orElse(null);
            extractedData = extractedResumeDataRepository.findByResumeId(latestResume.getId()).orElse(null);
        }

        List<JobMatchResult> recentMatches = jobMatchResultRepository.findByResumeUserIdOrderByMatchedAtDesc(userId);
        List<JobDto> savedJobs = jobService.getSavedJobs(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
        response.put("profile", profile);
        response.put("totalResumes", resumes.size());
        response.put("latestResume", latestResume != null ? new ResumeResponseDto(
                latestResume.getId(), latestResume.getOriginalFilename(), latestResume.getFileType(),
                latestResume.getFileSize(), latestResume.getVersion(), latestResume.getCreatedAt()
        ) : null);

        if (latestAnalysis != null) {
            ResumeAnalysisResponseDto aDto = new ResumeAnalysisResponseDto();
            aDto.setId(latestAnalysis.getId());
            aDto.setOverallScore(latestAnalysis.getOverallScore());
            aDto.setCompletenessScore(latestAnalysis.getCompletenessScore());
            aDto.setSkillCoverageScore(latestAnalysis.getSkillCoverageScore());
            aDto.setEducationScore(latestAnalysis.getEducationScore());
            aDto.setExperienceScore(latestAnalysis.getExperienceScore());
            aDto.setProjectScore(latestAnalysis.getProjectScore());
            aDto.setFormattingScore(latestAnalysis.getFormattingScore());
            aDto.setKeywordRelevanceScore(latestAnalysis.getKeywordRelevanceScore());
            aDto.setAnalyzedAt(latestAnalysis.getAnalyzedAt());

            try {
                if (latestAnalysis.getStrengthsJson() != null)
                    aDto.setStrengths(objectMapper.readValue(latestAnalysis.getStrengthsJson(), new TypeReference<>() {}));
                if (latestAnalysis.getWeaknessesJson() != null)
                    aDto.setWeaknesses(objectMapper.readValue(latestAnalysis.getWeaknessesJson(), new TypeReference<>() {}));
                if (latestAnalysis.getSuggestionsJson() != null)
                    aDto.setImprovementSuggestions(objectMapper.readValue(latestAnalysis.getSuggestionsJson(), new TypeReference<>() {}));
            } catch (Exception ignored) {}

            response.put("latestAnalysis", aDto);
        } else {
            response.put("latestAnalysis", null);
        }

        List<String> detectedSkills = new ArrayList<>();
        if (extractedData != null && extractedData.getTechnicalSkillsJson() != null) {
            try {
                detectedSkills = objectMapper.readValue(extractedData.getTechnicalSkillsJson(), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }
        response.put("detectedSkills", detectedSkills);

        // Recommendations
        if (latestResume != null && extractedData != null) {
            try {
                List<JobDto> recommended = jobService.getRecommendedJobs(latestResume.getId());
                response.put("recommendedJobs", recommended.stream().limit(5).toList());
            } catch (Exception e) {
                response.put("recommendedJobs", Collections.emptyList());
            }
        } else {
            response.put("recommendedJobs", Collections.emptyList());
        }

        response.put("savedJobsCount", savedJobs.size());
        response.put("recentMatchesCount", recentMatches.size());

        return response;
    }

    public StudentProfile getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(new StudentProfile(user)));
    }

    @Transactional
    public StudentProfile updateProfile(Long userId, StudentProfile updated) {
        StudentProfile profile = getProfile(userId);
        profile.setBio(updated.getBio());
        profile.setCurrentDegree(updated.getCurrentDegree());
        profile.setInstitution(updated.getInstitution());
        profile.setGraduationYear(updated.getGraduationYear());
        profile.setCgpa(updated.getCgpa());
        profile.setTargetJobTitle(updated.getTargetJobTitle());
        return studentProfileRepository.save(profile);
    }
}
