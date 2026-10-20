package com.resumeanalyzer.service;

import java.util.ArrayList;
import java.util.List;

public class SkillGapService {

    public static class RoadmapPhase {
        private int phaseNumber;
        private String title;
        private String description;
        private String estimatedDuration;
        private List<String> skills;
        private String resource;

        public RoadmapPhase(int phaseNumber, String title, String description, String estimatedDuration, List<String> skills, String resource) {
            this.phaseNumber = phaseNumber;
            this.title = title;
            this.description = description;
            this.estimatedDuration = estimatedDuration;
            this.skills = skills;
            this.resource = resource;
        }

        public int getPhaseNumber() { return phaseNumber; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getEstimatedDuration() { return estimatedDuration; }
        public List<String> getSkills() { return skills; }
        public String getResource() { return resource; }
    }

    public static class SkillGapReport {
        private String targetRole;
        private List<String> missingSkills = new ArrayList<>();
        private List<RoadmapPhase> roadmap = new ArrayList<>();
        private List<String> suggestedProjects = new ArrayList<>();

        public String getTargetRole() { return targetRole; }
        public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

        public List<String> getMissingSkills() { return missingSkills; }
        public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

        public List<RoadmapPhase> getRoadmap() { return roadmap; }
        public void setRoadmap(List<RoadmapPhase> roadmap) { this.roadmap = roadmap; }

        public List<String> getSuggestedProjects() { return suggestedProjects; }
        public void setSuggestedProjects(List<String> suggestedProjects) { this.suggestedProjects = suggestedProjects; }
    }

    public SkillGapReport generateGapReport(String targetRole, List<String> missingSkills) {
        SkillGapReport report = new SkillGapReport();
        report.setTargetRole(targetRole);
        report.setMissingSkills(missingSkills != null ? missingSkills : new ArrayList<>());

        List<RoadmapPhase> phases = new ArrayList<>();
        List<String> projects = new ArrayList<>();

        if (!report.getMissingSkills().isEmpty()) {
            List<String> p1Skills = new ArrayList<>();
            List<String> p2Skills = new ArrayList<>();
            List<String> p3Skills = new ArrayList<>();

            for (int i = 0; i < report.getMissingSkills().size(); i++) {
                String s = report.getMissingSkills().get(i);
                if (i % 3 == 0) p1Skills.add(s);
                else if (i % 3 == 1) p2Skills.add(s);
                else p3Skills.add(s);
            }

            if (!p1Skills.isEmpty()) {
                phases.add(new RoadmapPhase(1, "Core Foundation & Syntax",
                        "Master the syntax, runtime fundamentals, and idioms of your primary missing requirements.",
                        "2-3 Weeks", p1Skills, "Official Documentation, Exercism & Baeldung"));
            }
            if (!p2Skills.isEmpty()) {
                phases.add(new RoadmapPhase(2, "Framework Architecture & Data Persistence",
                        "Integrate frameworks, ORMs, and persistence mechanisms with testing suites.",
                        "3-4 Weeks", p2Skills, "Spring Guides, React Docs, Udemy Deep Dives"));
            }
            if (!p3Skills.isEmpty()) {
                phases.add(new RoadmapPhase(3, "Production Readiness & Cloud Containers",
                        "Implement containerization, automated pipelines, and cloud telemetry.",
                        "2-3 Weeks", p3Skills, "Docker Docs, Kubernetes The Hard Way, AWS Free Tier"));
            }

            phases.add(new RoadmapPhase(4, "System Design & Performance Tuning",
                    "Understand caching, database index optimization, distributed locking, and rate limiting.",
                    "2 Weeks", List.of("System Design", "Microservices", "Redis"), "Designing Data-Intensive Applications"));

            phases.add(new RoadmapPhase(5, "Interview Mastery & Mock Assessments",
                    "Solve algorithmic problems and conduct peer mock technical interviews.",
                    "1-2 Weeks", List.of("LeetCode", "Data Structures", "STAR Method"), "NeetCode 150 & Pramp"));

            for (String s : report.getMissingSkills()) {
                projects.add("Build a real-world showcase service utilizing " + s + " with REST endpoints, Docker, and unit tests.");
            }
        } else {
            // Already has all skills
            phases.add(new RoadmapPhase(1, "Advanced Architecture & System Design",
                    "Deep dive into high-scalability design patterns, microservices resilience, and rate limiting.",
                    "2-3 Weeks", List.of("System Design", "Microservices", "Design Patterns"), "High Scalability Blog & Martin Fowler"));
            phases.add(new RoadmapPhase(2, "Production Hardening & CI/CD",
                    "Implement automated testing suites, automated pipelines, and zero-downtime deployment.",
                    "2 Weeks", List.of("CI/CD", "Docker", "JUnit"), "GitHub Actions & Container Best Practices"));
            phases.add(new RoadmapPhase(3, "Open Source Contributions",
                    "Contribute bug fixes or documentation to popular open source repositories in your stack.",
                    "3 Weeks", List.of("Git", "Collaboration"), "GitHub Trending"));

            projects.add("Build an asynchronous event-driven microservices platform utilizing Kafka and Redis.");
            projects.add("Develop a full-stack real-time analytics dashboard with React, Spring Boot, and WebSockets.");
        }

        report.setRoadmap(phases);
        report.setSuggestedProjects(projects);
        return report;
    }
}
