package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private int overallScore;
    private int completenessScore;
    private int skillCoverageScore;
    private int educationScore;
    private int experienceScore;
    private int projectScore;
    private int formattingScore;
    private int keywordRelevanceScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String weaknessesJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String missingInfoJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String suggestionsJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    public ResumeAnalysis() {}

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

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

    public String getStrengthsJson() { return strengthsJson; }
    public void setStrengthsJson(String strengthsJson) { this.strengthsJson = strengthsJson; }

    public String getWeaknessesJson() { return weaknessesJson; }
    public void setWeaknessesJson(String weaknessesJson) { this.weaknessesJson = weaknessesJson; }

    public String getMissingInfoJson() { return missingInfoJson; }
    public void setMissingInfoJson(String missingInfoJson) { this.missingInfoJson = missingInfoJson; }

    public String getSuggestionsJson() { return suggestionsJson; }
    public void setSuggestionsJson(String suggestionsJson) { this.suggestionsJson = suggestionsJson; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}
