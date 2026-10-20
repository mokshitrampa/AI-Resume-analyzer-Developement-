package com.resumeanalyzer.service;

import com.resumeanalyzer.model.ExtractedResumeData;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResumeParserService {

    private final SkillExtractionService skillExtractionService = new SkillExtractionService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\+?\\d{10,13}");
    private static final Pattern CGPA_PATTERN = Pattern.compile("(?i)(?:cgpa|gpa|percentage|score)?:?\\s*(\\d{1,2}(?:\\.\\d{1,2})?)\\s*(?:/\\s*10|/\\s*4|%)?");
    private static final Pattern GRAD_YEAR_PATTERN = Pattern.compile("\\b(20[1-3][0-9])\\b");
    private static final Pattern DEGREE_PATTERN = Pattern.compile("(?i)\\b(bachelor|master|b\\.tech|m\\.tech|b\\.e|m\\.e|bca|mca|b\\.sc|m\\.sc|phd|associate)\\b(?:\\s+of\\s+[a-zA-Z\\s]+)?");

    public ExtractedResumeData parse(File file, Long resumeId) throws IOException {
        String filename = file.getName().toLowerCase();
        String rawText = "";

        if (filename.endsWith(".pdf")) {
            rawText = extractTextFromPdf(file);
        } else if (filename.endsWith(".docx")) {
            rawText = extractTextFromDocx(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: only PDF and DOCX are allowed.");
        }

        ExtractedResumeData data = new ExtractedResumeData();
        data.setResumeId(resumeId);
        data.setRawText(rawText);

        // Extract contact & academic fields
        data.setEmail(extractFirstMatch(EMAIL_PATTERN, rawText));
        data.setPhone(extractFirstMatch(PHONE_PATTERN, rawText));
        data.setCgpa(extractFirstMatch(CGPA_PATTERN, rawText));
        data.setDegree(extractFirstMatch(DEGREE_PATTERN, rawText));

        String gradYearStr = extractFirstMatch(GRAD_YEAR_PATTERN, rawText);
        if (gradYearStr != null) {
            try {
                data.setGraduationYear(Integer.parseInt(gradYearStr));
            } catch (NumberFormatException ignored) {}
        }

        // Infer Candidate Name (first line or before email)
        data.setCandidateName(inferCandidateName(rawText, data.getEmail()));

        // Technical skills via NLP Dictionary
        List<String> skills = skillExtractionService.extractSkills(rawText);
        data.setTechnicalSkills(skills);

        // Section extractions
        data.setExperience(extractSectionLines(rawText, "experience", "work history", "employment"));
        data.setEducation(extractSectionLines(rawText, "education", "academic", "qualifications"));
        data.setProjects(extractSectionLines(rawText, "projects", "academic projects", "notable projects"));
        data.setCertifications(extractSectionLines(rawText, "certifications", "licenses", "certificates"));

        return data;
    }

    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private String extractTextFromDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        }
    }

    private String extractFirstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    private String inferCandidateName(String text, String email) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.contains("@") || trimmed.contains("http") || trimmed.length() > 50) {
                continue;
            }
            if (trimmed.matches("^[A-Za-z\\s\\.\\-]+$") && trimmed.split("\\s+").length >= 2) {
                return trimmed;
            }
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@')).replace('.', ' ').toUpperCase();
        }
        return "Candidate";
    }

    private List<String> extractSectionLines(String text, String... headers) {
        List<String> results = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            boolean isHeader = false;
            for (String h : headers) {
                if (lower.contains(h) && trimmed.length() < 40) {
                    isHeader = true;
                    break;
                }
            }

            if (isHeader) {
                inSection = true;
                continue;
            } else if (inSection && trimmed.matches("^[A-Z\\s]{4,30}$") && trimmed.length() < 35) {
                // Next major header hit
                break;
            }

            if (inSection) {
                results.add(trimmed);
                if (results.size() >= 10) break;
            }
        }
        return results;
    }
}
