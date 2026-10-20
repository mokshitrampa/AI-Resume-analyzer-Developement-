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

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);

        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Resume latestResume = (resumes != null && !resumes.isEmpty()) ? resumes.get(0) : null;

        ResumeAnalysis latestAnalysis = null;
        ExtractedResumeData extractedData = null;
        if (latestResume != null) {
            latestAnalysis = resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(latestResume.getId()).orElse(null);
            extractedData = extractedResumeDataRepository.findByResumeId(latestResume.getId()).orElse(null);
        }

        List<JobMatchResult> recentMatches = Collections.emptyList();
        try {
            recentMatches = jobMatchResultRepository.findByResumeUserIdOrderByMatchedAtDesc(userId);
        } catch (Exception ignored) {}

        List<JobDto> savedJobs = Collections.emptyList();
        try {
            savedJobs = jobService.getSavedJobs(userId);
        } catch (Exception ignored) {}

        Map<String, Object> response = new HashMap<>();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", user.getFullName() != null ? user.getFullName() : "Student");
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("role", user.getRole() != null ? user.getRole().name() : "STUDENT");
        response.put("user", userMap);

        Map<String, Object> profileMap = new HashMap<>();
        if (profile != null) {
            profileMap.put("id", profile.getId());
            profileMap.put("bio", profile.getBio());
            profileMap.put("currentDegree", profile.getCurrentDegree());
            profileMap.put("institution", profile.getInstitution());
            profileMap.put("graduationYear", profile.getGraduationYear());
            profileMap.put("cgpa", profile.getCgpa());
            profileMap.put("targetJobTitle", profile.getTargetJobTitle());
            profileMap.put("resumeCount", profile.getResumeCount());
        }
        response.put("profile", profileMap);

        response.put("totalResumes", resumes != null ? resumes.size() : 0);
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

        response.put("savedJobsCount", savedJobs != null ? savedJobs.size() : 0);
        response.put("recentMatchesCount", recentMatches != null ? recentMatches.size() : 0);

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
