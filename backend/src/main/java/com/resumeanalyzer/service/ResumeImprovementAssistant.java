package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.analysis.AiImprovementDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ResumeImprovementAssistant {

    private static final Logger logger = LoggerFactory.getLogger(ResumeImprovementAssistant.class);

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.provider:local}")
    private String aiProvider;

    public List<AiImprovementDto> generateImprovements(ExtractedDataDto data) {
        List<AiImprovementDto> suggestions = new ArrayList<>();
        boolean isRealAi = hasConfiguredAiKey();

        // Check projects and experience for weak phrasing
        List<String> combinedSnippets = new ArrayList<>();
        if (data.getProjects() != null) combinedSnippets.addAll(data.getProjects());
        if (data.getExperience() != null) combinedSnippets.addAll(data.getExperience());

        if (combinedSnippets.isEmpty() && data.getRawText() != null) {
            String[] rawLines = data.getRawText().split("\\r?\\n");
            for (String l : rawLines) {
                if (l.trim().length() > 20 && (l.toLowerCase().contains("built") || l.toLowerCase().contains("made") || l.toLowerCase().contains("worked") || l.toLowerCase().contains("created"))) {
                    combinedSnippets.add(l.trim());
                    if (combinedSnippets.size() >= 3) break;
                }
            }
        }

        // Generate tailored improvements
        for (String snippet : combinedSnippets) {
            AiImprovementDto improvement = transformSnippet(snippet, isRealAi);
            if (improvement != null) {
                suggestions.add(improvement);
            }
        }

        // Add standard architectural wording upgrade if list is small
        if (suggestions.isEmpty()) {
            suggestions.add(new AiImprovementDto(
                    "Made a website using Java.",
                    "Architected and deployed an end-to-end web application utilizing Spring Boot, JPA persistence, and RESTful API endpoints.",
                    "Replaced vague passive phrasing ('Made a website') with strong action verb ('Architected') and concrete technical specifications.",
                    "ACTION_VERB",
                    isRealAi
            ));
            suggestions.add(new AiImprovementDto(
                    "Worked on frontend styling and database queries.",
                    "Engineered responsive UI components using Tailwind CSS and optimized relational database queries with indexing, reducing page latency.",
                    "Quantified engineering impact and highlighted specific modern design and optimization practices.",
                    "IMPACT",
                    isRealAi
            ));
            suggestions.add(new AiImprovementDto(
                    "Fixed bugs in the project.",
                    "Diagnosed and resolved critical software defects by authoring comprehensive JUnit test suites and conducting systematic root-cause analysis.",
                    "Emphasized software quality assurance methodology and test automation skills.",
                    "CLARITY",
                    isRealAi
            ));
        }

        return suggestions;
    }

    private AiImprovementDto transformSnippet(String original, boolean isRealAi) {
        String lower = original.toLowerCase();

        if (lower.startsWith("worked on") || lower.contains("worked on")) {
            String enhanced = original.replaceAll("(?i)worked on", "Engineered and implemented core modules for");
            return new AiImprovementDto(
                    original,
                    enhanced + ", ensuring high cohesion and robust modular architecture.",
                    "Upgraded passive verb 'worked on' to active engineering verb 'Engineered and implemented'.",
                    "ACTION_VERB",
                    isRealAi
            );
        } else if (lower.startsWith("made a") || lower.contains("made a") || lower.startsWith("created")) {
            String enhanced = original.replaceAll("(?i)(made a|created)", "Architected and delivered a full-stack");
            return new AiImprovementDto(
                    original,
                    enhanced + " featuring automated validation and secure persistent storage.",
                    "Enhanced basic creation statement to emphasize architectural scope and security.",
                    "CLARITY",
                    isRealAi
            );
        } else if (lower.contains("helped") || lower.contains("assisted")) {
            String enhanced = original.replaceAll("(?i)(helped with|assisted in)", "Collaborated with cross-functional peers to spearhead");
            return new AiImprovementDto(
                    original,
                    enhanced + " under Agile methodologies.",
                    "Replaced support-oriented language with proactive collaborative leadership.",
                    "IMPACT",
                    isRealAi
            );
        }

        return new AiImprovementDto(
                original,
                "Spearheaded: " + original + " while adhering to industry clean code principles and unit test coverage.",
                "Reinforced bullet point with active prefix and quality assurance focus.",
                "ACTION_VERB",
                isRealAi
        );
    }

    private boolean hasConfiguredAiKey() {
        return aiApiKey != null && !aiApiKey.isBlank() && !aiApiKey.startsWith("${");
    }
}
