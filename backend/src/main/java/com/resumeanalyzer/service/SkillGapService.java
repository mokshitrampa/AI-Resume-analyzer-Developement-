package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.dto.skillgap.RoadmapStepDto;
import com.resumeanalyzer.dto.skillgap.SkillGapResponseDto;
import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.SkillCategory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkillGapService {

    private final SkillExtractionService skillExtractionService;

    public SkillGapService(SkillExtractionService skillExtractionService) {
        this.skillExtractionService = skillExtractionService;
    }

    public SkillGapResponseDto analyzeSkillGap(Long resumeId, ExtractedDataDto extractedData, Job job) {
        SkillGapResponseDto dto = new SkillGapResponseDto();
        dto.setResumeId(resumeId);
        dto.setJobId(job != null ? job.getId() : null);
        dto.setTargetRole(job != null ? job.getTitle() : "Target Engineering Position");
        dto.setCompanyName(job != null ? job.getCompanyName() : "Selected Opportunity");

        // 1. Existing candidate skills
        Set<String> candidateSkills = new LinkedHashSet<>();
        if (extractedData.getTechnicalSkills() != null) {
            for (String s : extractedData.getTechnicalSkills()) {
                candidateSkills.add(skillExtractionService.getCanonicalName(s));
            }
        }
        if (extractedData.getRawText() != null) {
            for (String s : skillExtractionService.extractSkills(extractedData.getRawText())) {
                candidateSkills.add(skillExtractionService.getCanonicalName(s));
            }
        }
        dto.setExistingSkills(new ArrayList<>(candidateSkills));

        // 2. Required job skills
        Set<String> requiredSkills = new LinkedHashSet<>();
        if (job != null && job.getRequiredSkills() != null) {
            String[] split = job.getRequiredSkills().split("[,;\n]");
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    requiredSkills.add(skillExtractionService.getCanonicalName(trimmed));
                }
            }
        } else if (job != null && job.getDescription() != null) {
            requiredSkills.addAll(skillExtractionService.extractSkills(job.getDescription()));
        }
        dto.setRequiredSkills(new ArrayList<>(requiredSkills));

        // 3. Partition into Matching & Missing
        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String req : requiredSkills) {
            if (candidateSkills.contains(req)) {
                matching.add(req);
            } else {
                missing.add(req);
            }
        }
        dto.setMatchingSkills(matching);
        dto.setMissingSkills(missing);

        // 4. Prioritize missing skills by architectural tier (Language/Framework first, then DB, then Cloud/Devops)
        List<String> prioritySkills = new ArrayList<>(missing);
        prioritySkills.sort(Comparator.comparingInt(s -> {
            SkillCategory cat = skillExtractionService.getCategory(s);
            switch (cat) {
                case LANGUAGE: return 1;
                case FRAMEWORK: return 2;
                case DATABASE: return 3;
                case TOOL: return 4;
                case CLOUD: return 5;
                default: return 6;
            }
        }));
        dto.setPrioritySkills(prioritySkills);

        // 5. Generate Dynamic 5-Phase Roadmap based on actual gaps
        List<RoadmapStepDto> roadmap = generateRoadmap(prioritySkills, dto.getTargetRole());
        dto.setLearningRoadmap(roadmap);

        // 6. Generate Contextual Project Ideas
        List<String> projects = generateProjectIdeas(matching, missing, dto.getTargetRole());
        dto.setSuggestedProjects(projects);

        return dto;
    }

    private List<RoadmapStepDto> generateRoadmap(List<String> missingSkills, String targetRole) {
        List<RoadmapStepDto> steps = new ArrayList<>();

        if (missingSkills.isEmpty()) {
            steps.add(new RoadmapStepDto(1, "Advanced Architecture & System Design",
                    "Deep dive into high-scalability design patterns, microservices resilience, and rate limiting.",
                    Arrays.asList("System Design", "Microservices", "Design Patterns"), "2-3 Weeks", "High Scalability Blog & Martin Fowler"));
            steps.add(new RoadmapStepDto(2, "Production Hardening & CI/CD",
                    "Implement automated testing suites, automated pipelines, and zero-downtime deployment.",
                    Arrays.asList("CI/CD", "Docker", "JUnit"), "2 Weeks", "GitHub Actions & Container Best Practices"));
            steps.add(new RoadmapStepDto(3, "Open Source Contributions",
                    "Contribute bug fixes or documentation to popular open source repositories in your stack.",
                    Arrays.asList("Git", "Collaboration"), "3 Weeks", "GitHub Trending"));
            return steps;
        }

        // Divide missing skills across up to 5 phases
        int totalMissing = missingSkills.size();
        int phaseCount = Math.min(5, Math.max(3, totalMissing));

        int chunkSize = (int) Math.ceil((double) totalMissing / phaseCount);
        for (int i = 0; i < phaseCount; i++) {
            int start = i * chunkSize;
            if (start >= totalMissing) break;
            int end = Math.min(start + chunkSize, totalMissing);

            List<String> phaseSkills = missingSkills.subList(start, end);
            String primarySkill = phaseSkills.get(0);
            SkillCategory category = skillExtractionService.getCategory(primarySkill);

            String title;
            String desc;
            String resource;

            switch (category) {
                case LANGUAGE:
                    title = "Phase " + (i + 1) + ": Core Fundamentals of " + primarySkill;
                    desc = "Master language syntax, memory model, concurrency, and idiomatic data structures.";
                    resource = "Official Documentation & Interactive Tutorials";
                    break;
                case FRAMEWORK:
                    title = "Phase " + (i + 1) + ": Framework Mastery with " + String.join(", ", phaseSkills);
                    desc = "Build end-to-end components, implement routing, dependency injection, and security layers.";
                    resource = "Project-based Guided Workshops";
                    break;
                case DATABASE:
                    title = "Phase " + (i + 1) + ": Data Modeling & Persistence with " + String.join(", ", phaseSkills);
                    desc = "Design normalized relational or document schemas, write indexed queries, and manage transactions.";
                    resource = "Database Indexing & Query Optimization Guides";
                    break;
                case CLOUD:
                    title = "Phase " + (i + 1) + ": Cloud Containerization & Deployment (" + String.join(", ", phaseSkills) + ")";
                    desc = "Package multi-tier applications into containers and configure automated cluster orchestration.";
                    resource = "Docker & Cloud Architecture Sandboxes";
                    break;
                default:
                    title = "Phase " + (i + 1) + ": Integration & Tooling (" + String.join(", ", phaseSkills) + ")";
                    desc = "Incorporate modern development workflows, API testing, and observability.";
                    resource = "Developer Documentation & Reference Architecture";
                    break;
            }

            steps.add(new RoadmapStepDto(i + 1, title, desc, phaseSkills, "2-3 Weeks", resource));
        }

        return steps;
    }

    private List<String> generateProjectIdeas(List<String> matching, List<String> missing, String targetRole) {
        List<String> projects = new ArrayList<>();
        String primaryMissing = !missing.isEmpty() ? missing.get(0) : "Cloud Deployment";
        String secondaryMissing = missing.size() > 1 ? missing.get(1) : "REST API";

        projects.add(String.format("Build a %s showcase application incorporating %s with %s",
                targetRole, primaryMissing, secondaryMissing));
        projects.add(String.format("Develop a distributed asynchronous microservice utilizing %s and database caching",
                primaryMissing));
        projects.add(String.format("Create an automated CI/CD deployment pipeline with Docker containers featuring %s integration",
                !missing.isEmpty() ? missing.get(0) : "production telemetry"));

        return projects;
    }
}
