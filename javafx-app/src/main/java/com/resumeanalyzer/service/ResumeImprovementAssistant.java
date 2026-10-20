package com.resumeanalyzer.service;

import java.util.ArrayList;
import java.util.List;

public class ResumeImprovementAssistant {

    public static class PhrasingSuggestion {
        private String originalBullet;
        private String suggestedBullet;
        private String rationale;
        private String impactFactor; // High, Medium

        public PhrasingSuggestion(String originalBullet, String suggestedBullet, String rationale, String impactFactor) {
            this.originalBullet = originalBullet;
            this.suggestedBullet = suggestedBullet;
            this.rationale = rationale;
            this.impactFactor = impactFactor;
        }

        public String getOriginalBullet() { return originalBullet; }
        public String getSuggestedBullet() { return suggestedBullet; }
        public String getRationale() { return rationale; }
        public String getImpactFactor() { return impactFactor; }
    }

    public List<PhrasingSuggestion> generateSuggestions(List<String> rawBullets) {
        List<PhrasingSuggestion> list = new ArrayList<>();

        if (rawBullets == null || rawBullets.isEmpty()) {
            list.add(new PhrasingSuggestion(
                    "Worked on backend APIs using Spring Boot and SQL",
                    "Architected high-throughput RESTful microservices in Spring Boot, reducing API latency by 35% across 50,000 daily requests.",
                    "Replaced passive verb with high-impact action verb and quantifiable business metrics.",
                    "High"
            ));
            list.add(new PhrasingSuggestion(
                    "Responsible for creating frontend pages in React",
                    "Engineered responsive, accessible React Single Page Applications with Redux state management, improving customer session duration by 28%.",
                    "Eliminated 'responsible for' cliché and highlighted technical breadth with measurable customer impact.",
                    "High"
            ));
            list.add(new PhrasingSuggestion(
                    "Did bug fixes and maintained database",
                    "Optimized MySQL database queries and automated CI/CD deployment pipelines using Docker, cutting deployment failure rates by 40%.",
                    "Demonstrates proactive performance engineering and automated infrastructure ownership.",
                    "Medium"
            ));
            return list;
        }

        for (String bullet : rawBullets) {
            if (bullet.length() > 15) {
                String clean = bullet.replaceAll("^[-•*]\\s*", "").trim();
                list.add(new PhrasingSuggestion(
                        clean,
                        "Engineered scalable solution: " + clean + ", achieving 30% performance boost and ensuring 99.9% uptime.",
                        "Enriched bullet with quantifiable impact metrics, action verb, and production reliability indicators.",
                        "High"
                ));
                if (list.size() >= 4) break;
            }
        }

        if (list.isEmpty()) {
            list.add(new PhrasingSuggestion(
                    "Participated in software development projects",
                    "Spearheaded design and delivery of distributed cloud microservices, accelerating feature release cycles by 2.5x.",
                    "Replaced weak passive verb with impactful leadership action verb.",
                    "High"
            ));
        }

        return list;
    }
}
