package com.resumeanalyzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResumeAnalysis {
    private Long id;
    private Long resumeId;
    private int overallScore;
    private int completenessScore;
    private int skillCoverageScore;
    private int educationScore;
    private int experienceScore;
    private int projectScore;
    private int formattingScore;
    private int keywordRelevanceScore;

    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    private List<String> missingInfo = new ArrayList<>();
    private List<String> improvementSuggestions = new ArrayList<>();
    private LocalDateTime analyzedAt;

    public ResumeAnalysis() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public int getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(int completenessScore) { this.completenessScore = completenessScore; }

    public int getSkillCoverageScore() { return skillCoverageScore; }
    public void setSkillCoverageScore(int skillCoverageScore) { this.skillCoverageScore = skillCoverageScore; }

    public int getEducationScore() { return educationScore; }
    public void setEducationScore(int educationScore) { this.educationScore = educationScore; }

    public int getExperienceScore() { return experienceScore; }
    public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }

    public int getProjectScore() { return projectScore; }
    public void setProjectScore(int projectScore) { this.projectScore = projectScore; }

    public int getFormattingScore() { return formattingScore; }
    public void setFormattingScore(int formattingScore) { this.formattingScore = formattingScore; }

    public int getKeywordRelevanceScore() { return keywordRelevanceScore; }
    public void setKeywordRelevanceScore(int keywordRelevanceScore) { this.keywordRelevanceScore = keywordRelevanceScore; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths != null ? strengths : new ArrayList<>(); }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>(); }

    public List<String> getMissingInfo() { return missingInfo; }
    public void setMissingInfo(List<String> missingInfo) { this.missingInfo = missingInfo != null ? missingInfo : new ArrayList<>(); }

    public List<String> getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(List<String> improvementSuggestions) { this.improvementSuggestions = improvementSuggestions != null ? improvementSuggestions : new ArrayList<>(); }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
