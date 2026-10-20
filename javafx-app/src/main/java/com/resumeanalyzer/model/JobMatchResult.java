package com.resumeanalyzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobMatchResult {
    private Long id;
    private Long resumeId;
    private Long jobId;
    private String jobTitle;
    private String companyName;

    private int overallScore;
    private int requiredSkillsScore;
    private int preferredSkillsScore;
    private int semanticScore;
    private int experienceScore;
    private int educationScore;

    private List<String> matchingSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<ScoreComponent> components = new ArrayList<>();
    private String explanation;
    private LocalDateTime matchedAt;

    public static class ScoreComponent {
        private String name;
        private int rawScore;
        private double effectiveWeightPercentage;
        private double contributedPoints;

        public ScoreComponent() {}

        public ScoreComponent(String name, int rawScore, double effectiveWeightPercentage, double contributedPoints) {
            this.name = name;
            this.rawScore = rawScore;
            this.effectiveWeightPercentage = effectiveWeightPercentage;
            this.contributedPoints = contributedPoints;
        }

        public String getName() { return name; }
        public int getRawScore() { return rawScore; }
        public double getEffectiveWeightPercentage() { return effectiveWeightPercentage; }
        public double getContributedPoints() { return contributedPoints; }
    }

    public JobMatchResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

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

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills != null ? matchingSkills : new ArrayList<>(); }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>(); }

    public List<ScoreComponent> getComponents() { return components; }
    public void setComponents(List<ScoreComponent> components) { this.components = components != null ? components : new ArrayList<>(); }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
}
