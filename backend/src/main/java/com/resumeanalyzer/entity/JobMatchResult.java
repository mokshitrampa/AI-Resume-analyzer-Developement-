package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_match_results")
public class JobMatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(length = 150)
    private String customJobTitle;

    private int overallScore;
    private int requiredSkillsScore;
    private int preferredSkillsScore;
    private int semanticScore;
    private int experienceScore;
    private int educationScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String matchingSkillsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String missingSkillsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    public JobMatchResult() {}

    @PrePersist
    protected void onCreate() {
        this.matchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public String getCustomJobTitle() { return customJobTitle; }
    public void setCustomJobTitle(String customJobTitle) { this.customJobTitle = customJobTitle; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public int getRequiredSkillsScore() { return requiredSkillsScore; }
    public void setRequiredSkillsScore(int requiredSkillsScore) { this.requiredSkillsScore = requiredSkillsScore; }

    public int getPreferredSkillsScore() { return preferredSkillsScore; }
    public void setPreferredSkillsScore(int preferredSkillsScore) { this.preferredSkillsScore = preferredSkillsScore; }

    public int getSemanticScore() { return semanticScore; }
    public void setSemanticScore(int semanticScore) { this.semanticScore = semanticScore; }

    public int getExperienceScore() { return experienceScore; }
    public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }

    public int getEducationScore() { return educationScore; }
    public void setEducationScore(int educationScore) { this.educationScore = educationScore; }

    public String getMatchingSkillsJson() { return matchingSkillsJson; }
    public void setMatchingSkillsJson(String matchingSkillsJson) { this.matchingSkillsJson = matchingSkillsJson; }

    public String getMissingSkillsJson() { return missingSkillsJson; }
    public void setMissingSkillsJson(String missingSkillsJson) { this.missingSkillsJson = missingSkillsJson; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getMatchedAt() { return matchedAt; }
}
