package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.analysis.ResumeAnalysisResponseDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeAnalysisService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalysisService(ResumeAnalysisRepository resumeAnalysisRepository) {
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    @Transactional
    public ResumeAnalysisResponseDto analyzeQuality(Resume resume, ExtractedDataDto data) {
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // 1. Completeness Score (0 - 100)
        int completeness = 0;
        if (data.getCandidateName() != null && !data.getCandidateName().isBlank()) completeness += 20;
        else missingInfo.add("Candidate Full Name is missing or undetectable");

        if (data.getEmail() != null && !data.getEmail().isBlank()) completeness += 20;
        else missingInfo.add("Email address is missing");

        if (data.getPhone() != null && !data.getPhone().isBlank()) completeness += 15;
        else missingInfo.add("Phone number is missing");

        if (data.getDegree() != null || (data.getEducation() != null && !data.getEducation().isEmpty())) completeness += 25;
        else missingInfo.add("Education credentials section is missing");

        if (data.getTechnicalSkills() != null && !data.getTechnicalSkills().isEmpty()) completeness += 20;
        else missingInfo.add("Technical skills section is missing or empty");

        // 2. Technical Skill Coverage Score (0 - 100)
        int skillCount = data.getTechnicalSkills() != null ? data.getTechnicalSkills().size() : 0;
        int skillCoverage = Math.min(100, skillCount * 12);
        if (skillCount >= 6) {
            strengths.add("Strong technical skill breadth with " + skillCount + " verified technologies detected");
        } else {
            weaknesses.add("Limited technical skills detected (" + skillCount + "). Aim for 6-12 relevant technologies.");
            suggestions.add("Add specialized libraries, testing frameworks, and databases you have used in your coursework or projects.");
        }

        // 3. Education Score (0 - 100)
        int educationScore = 30;
        if (data.getDegree() != null) educationScore += 30;
        if (data.getGraduationYear() != null) educationScore += 20;
        if (data.getCgpa() != null) educationScore += 20;
        educationScore = Math.min(100, educationScore);
        if (data.getCgpa() != null) {
            strengths.add("Academic performance / CGPA clearly documented (" + data.getCgpa() + ")");
        } else {
            suggestions.add("Include your current CGPA/GPA if it showcases positive academic standing.");
        }

        // 4. Project Score (0 - 100)
        int projectCount = data.getProjects() != null ? data.getProjects().size() : 0;
        int projectScore = Math.min(100, Math.max(25, projectCount * 30));
        if (projectCount >= 2) {
            strengths.add("Includes dedicated project portfolio section showcasing applied development experience");
        } else {
            weaknesses.add("Few or unstructured project descriptions found");
            suggestions.add("Describe 2 to 3 key capstone or lab projects detailing problem statement, stack used, and measurable outcome.");
        }

        // 5. Work Experience Score (0 - 100)
        int expCount = data.getExperience() != null ? data.getExperience().size() : 0;
        int experienceScore = Math.min(100, Math.max(30, expCount * 35));
        if (expCount > 0) {
            strengths.add("Practical internship or engineering work experience highlighted");
        } else {
            suggestions.add("Include open-source contributions, academic research, or technical leadership roles to demonstrate practical teamwork.");
        }

        // 6. Formatting & Readability Score (0 - 100)
        int rawLength = data.getRawText() != null ? data.getRawText().length() : 0;
        int formattingScore = 75;
        if (rawLength > 600 && rawLength < 4000) {
            formattingScore = 90;
            strengths.add("Document length and text density conform to standard 1-2 page hiring conventions");
        } else if (rawLength <= 600) {
            formattingScore = 45;
            weaknesses.add("Resume appears unusually brief. Key technical descriptions may be omitted.");
        } else {
            formattingScore = 65;
            weaknesses.add("Resume text is very lengthy; consider trimming older or non-technical sections.");
        }

        // 7. Keyword Relevance Score (0 - 100)
        int keywordScore = Math.min(100, Math.max(40, skillCount * 10 + projectCount * 10));

        // Deterministic Weighted Overall Score
        // Completeness: 20%, Skills: 25%, Projects: 20%, Education: 15%, Experience: 10%, Formatting: 10%
        double totalWeighted = (completeness * 0.20) +
                               (skillCoverage * 0.25) +
                               (projectScore * 0.20) +
                               (educationScore * 0.15) +
                               (experienceScore * 0.10) +
                               (formattingScore * 0.10);

        int overallScore = (int) Math.round(Math.max(0, Math.min(100, totalWeighted)));

        // Persist analysis entity
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(resume);
        analysis.setOverallScore(overallScore);
        analysis.setCompletenessScore(completeness);
        analysis.setSkillCoverageScore(skillCoverage);
        analysis.setEducationScore(educationScore);
        analysis.setExperienceScore(experienceScore);
        analysis.setProjectScore(projectScore);
        analysis.setFormattingScore(formattingScore);
        analysis.setKeywordRelevanceScore(keywordScore);

        try {
            analysis.setStrengthsJson(objectMapper.writeValueAsString(strengths));
            analysis.setWeaknessesJson(objectMapper.writeValueAsString(weaknesses));
            analysis.setMissingInfoJson(objectMapper.writeValueAsString(missingInfo));
            analysis.setSuggestionsJson(objectMapper.writeValueAsString(suggestions));
        } catch (Exception ignored) {}

        ResumeAnalysis saved = resumeAnalysisRepository.save(analysis);

        ResumeAnalysisResponseDto dto = new ResumeAnalysisResponseDto();
        dto.setId(saved.getId());
        dto.setResumeId(resume.getId());
        dto.setOverallScore(saved.getOverallScore());
        dto.setCompletenessScore(saved.getCompletenessScore());
        dto.setSkillCoverageScore(saved.getSkillCoverageScore());
        dto.setEducationScore(saved.getEducationScore());
        dto.setExperienceScore(saved.getExperienceScore());
        dto.setProjectScore(saved.getProjectScore());
        dto.setFormattingScore(saved.getFormattingScore());
        dto.setKeywordRelevanceScore(saved.getKeywordRelevanceScore());
        dto.setStrengths(strengths);
        dto.setWeaknesses(weaknesses);
        dto.setMissingInfo(missingInfo);
        dto.setImprovementSuggestions(suggestions);
        dto.setAnalyzedAt(saved.getAnalyzedAt());

        return dto;
    }
}
