package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.algorithm.CosineSimilarity;
import com.resumeanalyzer.algorithm.WeightedScoreCalculator;
import com.resumeanalyzer.dto.matching.ComponentScoreDto;
import com.resumeanalyzer.dto.matching.JobMatchResponseDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.JobMatchResult;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.repository.JobMatchResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingEngine {

    private final SkillExtractionService skillExtractionService;
    private final JobMatchResultRepository jobMatchResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobMatchingEngine(SkillExtractionService skillExtractionService,
                             JobMatchResultRepository jobMatchResultRepository) {
        this.skillExtractionService = skillExtractionService;
        this.jobMatchResultRepository = jobMatchResultRepository;
    }

    @Transactional
    public JobMatchResponseDto matchResumeWithJob(Resume resume, ExtractedDataDto extractedData, Job job) {
        return executeMatch(resume, extractedData, job.getId(), job.getTitle(), job.getCompanyName(),
                job.getDescription(), job.getRequiredSkills(), job.getPreferredSkills(), job.getExperienceLevel());
    }

    @Transactional
    public JobMatchResponseDto matchResumeWithCustomText(Resume resume, ExtractedDataDto extractedData,
                                                         String jobTitle, String jobDescription,
                                                         String requiredSkills, String preferredSkills) {
        return executeMatch(resume, extractedData, null, jobTitle, "Custom Job Description",
                jobDescription, requiredSkills, preferredSkills, null);
    }

    private JobMatchResponseDto executeMatch(Resume resume, ExtractedDataDto extractedData,
                                             Long jobId, String jobTitle, String companyName,
                                             String jobDescription, String rawReqSkills, String rawPrefSkills,
                                             String experienceLevel) {

        // 1. Candidate skills (canonicalized)
        Set<String> candidateSkills = new HashSet<>();
        if (extractedData.getTechnicalSkills() != null) {
            for (String s : extractedData.getTechnicalSkills()) {
                candidateSkills.add(skillExtractionService.getCanonicalName(s));
            }
        }
        // Also scan resume raw text for any missed skills
        if (extractedData.getRawText() != null) {
            for (String s : skillExtractionService.extractSkills(extractedData.getRawText())) {
                candidateSkills.add(skillExtractionService.getCanonicalName(s));
            }
        }

        // 2. Parse required skills
        List<String> requiredSkillList = parseSkillList(rawReqSkills, jobDescription);
        boolean hasReqSkills = !requiredSkillList.isEmpty();

        // 3. Parse preferred skills
        List<String> preferredSkillList = parseSkillList(rawPrefSkills, null);
        boolean hasPrefSkills = !preferredSkillList.isEmpty();

        // 4. Calculate Overlaps
        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        int matchedReqCount = 0;
        for (String req : requiredSkillList) {
            String canonicalReq = skillExtractionService.getCanonicalName(req);
            if (candidateSkills.contains(canonicalReq)) {
                matchedReqCount++;
                if (!matchingSkills.contains(canonicalReq)) matchingSkills.add(canonicalReq);
            } else {
                if (!missingSkills.contains(canonicalReq)) missingSkills.add(canonicalReq);
            }
        }

        int matchedPrefCount = 0;
        for (String pref : preferredSkillList) {
            String canonicalPref = skillExtractionService.getCanonicalName(pref);
            if (candidateSkills.contains(canonicalPref)) {
                matchedPrefCount++;
                if (!matchingSkills.contains(canonicalPref)) matchingSkills.add(canonicalPref);
            } else {
                if (!missingSkills.contains(canonicalPref)) missingSkills.add(canonicalPref);
            }
        }

        int reqSkillScore = hasReqSkills ? (int) Math.round(((double) matchedReqCount / requiredSkillList.size()) * 100.0) : 100;
        int prefSkillScore = hasPrefSkills ? (int) Math.round(((double) matchedPrefCount / preferredSkillList.size()) * 100.0) : 100;

        // 5. Semantic / TF-IDF Cosine Similarity
        String resumeText = extractedData.getRawText() != null ? extractedData.getRawText() : "";
        String fullJobText = (jobTitle != null ? jobTitle + " " : "") +
                (jobDescription != null ? jobDescription + " " : "") +
                (rawReqSkills != null ? rawReqSkills : "");
        int semanticScore = CosineSimilarity.calculatePercentage(resumeText, fullJobText);

        // 6. Experience & Education heuristics
        boolean hasExpReq = experienceLevel != null && !experienceLevel.isBlank();
        int experienceScore = 75; // Default solid baseline for student projects/internships
        if (extractedData.getExperience() != null && !extractedData.getExperience().isEmpty()) {
            experienceScore = 90;
        }

        boolean hasEduReq = true;
        int educationScore = 80;
        if (extractedData.getDegree() != null || (extractedData.getEducation() != null && !extractedData.getEducation().isEmpty())) {
            educationScore = 95;
        }

        // 7. Weighted Calculation with automatic weight redistribution
        WeightedScoreCalculator.CalculationResult calcResult = WeightedScoreCalculator.calculate(
                reqSkillScore, hasReqSkills,
                prefSkillScore, hasPrefSkills,
                semanticScore,
                experienceScore, hasExpReq,
                educationScore, hasEduReq
        );

        // 8. Persist Result
        JobMatchResult matchResult = new JobMatchResult();
        matchResult.setResume(resume);
        if (jobId != null) {
            Job jobRef = new Job();
            jobRef.setId(jobId);
            matchResult.setJob(jobRef);
        }
        matchResult.setCustomJobTitle(jobTitle);
        matchResult.setOverallScore(calcResult.getOverallScore());
        matchResult.setRequiredSkillsScore(reqSkillScore);
        matchResult.setPreferredSkillsScore(prefSkillScore);
        matchResult.setSemanticScore(semanticScore);
        matchResult.setExperienceScore(experienceScore);
        matchResult.setEducationScore(educationScore);
        matchResult.setExplanation(calcResult.getExplanation());

        try {
            matchResult.setMatchingSkillsJson(objectMapper.writeValueAsString(matchingSkills));
            matchResult.setMissingSkillsJson(objectMapper.writeValueAsString(missingSkills));
        } catch (Exception ignored) {}

        JobMatchResult saved = jobMatchResultRepository.save(matchResult);

        // 9. Map to DTO
        JobMatchResponseDto dto = new JobMatchResponseDto();
        dto.setId(saved.getId());
        dto.setResumeId(resume.getId());
        dto.setJobId(jobId);
        dto.setJobTitle(jobTitle);
        dto.setCompanyName(companyName);
        dto.setOverallScore(saved.getOverallScore());
        dto.setRequiredSkillsScore(reqSkillScore);
        dto.setPreferredSkillsScore(prefSkillScore);
        dto.setSemanticScore(semanticScore);
        dto.setExperienceScore(experienceScore);
        dto.setEducationScore(educationScore);
        dto.setMatchingSkills(matchingSkills);
        dto.setMissingSkills(missingSkills);
        dto.setExplanation(saved.getExplanation());
        dto.setMatchedAt(saved.getMatchedAt());

        List<ComponentScoreDto> componentDtos = new ArrayList<>();
        for (WeightedScoreCalculator.ComponentScore c : calcResult.getComponents()) {
            componentDtos.add(new ComponentScoreDto(
                    c.getName(),
                    c.rawScore(),
                    Math.round(c.getEffectiveWeight() * 1000.0) / 10.0,
                    Math.round(c.getContributedScore() * 10.0) / 10.0
            ));
        }
        dto.setComponents(componentDtos);

        return dto;
    }

    private List<String> parseSkillList(String rawSkills, String fallbackText) {
        Set<String> skills = new LinkedHashSet<>();
        if (rawSkills != null && !rawSkills.isBlank()) {
            String[] split = rawSkills.split("[,;\n]");
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    skills.add(skillExtractionService.getCanonicalName(trimmed));
                }
            }
        } else if (fallbackText != null && !fallbackText.isBlank()) {
            skills.addAll(skillExtractionService.extractSkills(fallbackText));
        }
        return new ArrayList<>(skills);
    }
}
