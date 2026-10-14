package com.resumeanalyzer.dto.skillgap;

import java.util.ArrayList;
import java.util.List;

public class SkillGapResponseDto {

    private Long resumeId;
    private Long jobId;
    private String targetRole;
    private String companyName;

    private List<String> existingSkills = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();
    private List<String> matchingSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> prioritySkills = new ArrayList<>();
    private List<RoadmapStepDto> learningRoadmap = new ArrayList<>();
    private List<String> suggestedProjects = new ArrayList<>();

    public SkillGapResponseDto() {}

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public List<String> getExistingSkills() { return existingSkills; }
    public void setExistingSkills(List<String> existingSkills) { this.existingSkills = existingSkills; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getPrioritySkills() { return prioritySkills; }
    public void setPrioritySkills(List<String> prioritySkills) { this.prioritySkills = prioritySkills; }

    public List<RoadmapStepDto> getLearningRoadmap() { return learningRoadmap; }
    public void setLearningRoadmap(List<RoadmapStepDto> learningRoadmap) { this.learningRoadmap = learningRoadmap; }

    public List<String> getSuggestedProjects() { return suggestedProjects; }
    public void setSuggestedProjects(List<String> suggestedProjects) { this.suggestedProjects = suggestedProjects; }
}
