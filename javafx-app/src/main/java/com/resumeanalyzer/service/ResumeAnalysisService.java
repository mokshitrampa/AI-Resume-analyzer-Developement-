package com.resumeanalyzer.service;

import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.ResumeAnalysis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResumeAnalysisService {

    public ResumeAnalysis analyze(ExtractedResumeData data) {
        ResumeAnalysis a = new ResumeAnalysis();
        a.setResumeId(data.getResumeId());

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // 1. Completeness Score (max 20 pts)
        int completeness = 0;
        if (data.getCandidateName() != null && !data.getCandidateName().isEmpty()) completeness += 4;
        else missing.add("Candidate Full Name");

        if (data.getEmail() != null && !data.getEmail().isEmpty()) completeness += 4;
        else missing.add("Contact Email Address");

        if (data.getPhone() != null && !data.getPhone().isEmpty()) completeness += 3;
        else missing.add("Phone Number");

        if (data.getDegree() != null && !data.getDegree().isEmpty()) completeness += 3;
        else missing.add("Education Degree");

        if (!data.getTechnicalSkills().isEmpty()) completeness += 3;
        else missing.add("Technical Skills Section");

        if (!data.getProjects().isEmpty() || !data.getExperience().isEmpty()) completeness += 3;
        else missing.add("Work Experience or Projects Section");

        a.setCompletenessScore((int) Math.round((completeness / 20.0) * 100));

        // 2. Technical Skill Breadth (max 20 pts)
        int skillCount = data.getTechnicalSkills().size();
        int skillScore;
        if (skillCount >= 15) {
            skillScore = 100;
            strengths.add("Exceptional technical breadth with " + skillCount + " verified technologies detected.");
        } else if (skillCount >= 10) {
            skillScore = 85;
            strengths.add("Strong technical foundation with " + skillCount + " recognized skills.");
        } else if (skillCount >= 5) {
            skillScore = 70;
            suggestions.add("Broaden your listed stack with modern databases, DevOps tools, or cloud platforms.");
        } else {
            skillScore = 40;
            weaknesses.add("Limited technical skills detected (" + skillCount + " skills).");
            suggestions.add("Add frameworks, languages, databases, and CI/CD tools you have experience with.");
        }
        a.setSkillCoverageScore(skillScore);

        // 3. Education Score (max 15 pts)
        int eduScore = 50;
        if (data.getDegree() != null && !data.getDegree().isEmpty()) {
            eduScore += 25;
            strengths.add("Academic degree clearly stated: " + data.getDegree());
        }
        if (data.getCgpa() != null && !data.getCgpa().isEmpty()) {
            eduScore += 25;
            strengths.add("Academic performance / CGPA recorded (" + data.getCgpa() + ").");
        } else {
            suggestions.add("Consider including your GPA/CGPA if above 7.0/10 or 3.0/4.0.");
        }
        a.setEducationScore(Math.min(100, eduScore));

        // 4. Experience Score (max 15 pts)
        int expCount = data.getExperience().size();
        int expScore;
        if (expCount >= 3) {
            expScore = 95;
            strengths.add("Practical engineering experience or internship history documented.");
        } else if (expCount >= 1) {
            expScore = 80;
            suggestions.add("Add quantified business impact metrics to your experience bullet points.");
        } else {
            expScore = 50;
            weaknesses.add("Minimal formal work experience lines detected.");
            suggestions.add("Highlight internships, freelance contracts, or open source contributions.");
        }
        a.setExperienceScore(expScore);

        // 5. Applied Project Score (max 15 pts)
        int projCount = data.getProjects().size();
        int projScore;
        if (projCount >= 2) {
            projScore = 90;
            strengths.add("Dedicated portfolio section showcasing applied development projects.");
        } else if (projCount == 1) {
            projScore = 70;
            suggestions.add("Include at least 2 to 3 distinct software projects with live URLs or GitHub links.");
        } else {
            projScore = 45;
            weaknesses.add("No distinct projects section detected.");
            suggestions.add("Add notable technical projects highlighting architecture, libraries, and outcomes.");
        }
        a.setProjectScore(projScore);

        // 6. Formatting & Readability Score (max 10 pts)
        int wordCount = data.getRawText() != null ? data.getRawText().split("\\s+").length : 0;
        int formatScore;
        if (wordCount >= 250 && wordCount <= 1000) {
            formatScore = 95;
            strengths.add("Optimal resume length (" + wordCount + " words), conforming to standard hiring conventions.");
        } else if (wordCount > 1000) {
            formatScore = 75;
            suggestions.add("Resume may be overly dense (" + wordCount + " words). Aim for a concise 1-2 page format.");
        } else {
            formatScore = 60;
            weaknesses.add("Resume text is very brief (" + wordCount + " words). Expand project and skill descriptions.");
        }
        a.setFormattingScore(formatScore);

        // 7. Keyword Relevance Score (max 5 pts)
        int keywordScore = 80;
        String rawLower = data.getRawText() != null ? data.getRawText().toLowerCase() : "";
        if (rawLower.contains("microservices") || rawLower.contains("api") || rawLower.contains("cloud") || rawLower.contains("docker")) {
            keywordScore = 100;
        }
        a.setKeywordRelevanceScore(keywordScore);

        // Overall Weighted Score (0 to 100)
        double overall = (a.getCompletenessScore() * 0.20)
                + (a.getSkillCoverageScore() * 0.20)
                + (a.getEducationScore() * 0.15)
                + (a.getExperienceScore() * 0.15)
                + (a.getProjectScore() * 0.15)
                + (a.getFormattingScore() * 0.10)
                + (a.getKeywordRelevanceScore() * 0.05);

        a.setOverallScore((int) Math.round(overall));
        a.setStrengths(strengths);
        a.setWeaknesses(weaknesses);
        a.setMissingInfo(missing);
        a.setImprovementSuggestions(suggestions);
        a.setAnalyzedAt(LocalDateTime.now());

        return a;
    }
}
