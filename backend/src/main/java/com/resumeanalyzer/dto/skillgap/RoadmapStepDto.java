package com.resumeanalyzer.dto.skillgap;

import java.util.List;

public class RoadmapStepDto {

    private int phase;
    private String title;
    private String description;
    private List<String> skills;
    private String estimatedDuration;
    private String resourceSuggestion;

    public RoadmapStepDto() {}

    public RoadmapStepDto(int phase, String title, String description, List<String> skills, String estimatedDuration, String resourceSuggestion) {
        this.phase = phase;
        this.title = title;
        this.description = description;
        this.skills = skills;
        this.estimatedDuration = estimatedDuration;
        this.resourceSuggestion = resourceSuggestion;
    }

    public int getPhase() { return phase; }
    public void setPhase(int phase) { this.phase = phase; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getResourceSuggestion() { return resourceSuggestion; }
    public void setResourceSuggestion(String resourceSuggestion) { this.resourceSuggestion = resourceSuggestion; }
}
