package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{3}\\)?[-.\\s]?)?\\d{3}[-.\\s]?\\d{4}");

    private static final Pattern CGPA_PATTERN =
            Pattern.compile("(?:cgpa|gpa|percentage)\\s*[:=-]?\\s*([0-9]+(?:\\.[0-9]+)?(?:/10|/4|%)?)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GRAD_YEAR_PATTERN =
            Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");

    private static final List<String> DEGREE_KEYWORDS = Arrays.asList(
            "b.tech", "b.e", "bachelor of technology", "bachelor of engineering", "b.sc", "b.s.", "bachelor of science",
            "m.tech", "m.e", "master of technology", "m.sc", "m.s.", "master of science", "mca", "bca", "ph.d", "doctorate"
    );

    public String extractRawText(File file, String fileType) {
        if (!file.exists()) {
            throw new BadRequestException("Resume document file not found on server");
        }

        try {
            if ("pdf".equalsIgnoreCase(fileType)) {
                try (PDDocument document = Loader.loadPDF(file)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    if (text == null || text.trim().isEmpty()) {
                        logger.warn("PDFBox extracted 0 text characters from file: {}", file.getName());
                        return "Note: Scanned or image-only PDF detected. Please upload a text-searchable PDF for complete automated extraction.";
                    }
                    return text.trim();
                }
            } else if ("docx".equalsIgnoreCase(fileType)) {
                try (InputStream is = new FileInputStream(file);
                     XWPFDocument doc = new XWPFDocument(is);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    String text = extractor.getText();
                    if (text == null || text.trim().isEmpty()) {
                        return "Note: Document contains no extractable text.";
                    }
                    return text.trim();
                }
            } else {
                throw new BadRequestException("Unsupported file type: " + fileType);
            }
        } catch (Exception ex) {
            logger.error("Error reading document content", ex);
            throw new BadRequestException("Failed to extract text from document: " + ex.getMessage());
        }
    }

    public ExtractedDataDto parseResumeDetails(String rawText) {
        ExtractedDataDto dto = new ExtractedDataDto();
        dto.setRawText(rawText);

        if (rawText == null || rawText.isBlank()) {
            return dto;
        }

        String[] lines = rawText.split("\\r?\\n");

        // 1. Candidate Name heuristic (first non-empty line without email/phone/symbols)
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 2 && trimmed.length() < 40 &&
                    !EMAIL_PATTERN.matcher(trimmed).find() &&
                    !PHONE_PATTERN.matcher(trimmed).find() &&
                    !trimmed.toLowerCase().contains("resume") &&
                    !trimmed.toLowerCase().contains("curriculum") &&
                    !trimmed.toLowerCase().contains("page")) {
                dto.setCandidateName(trimmed);
                break;
            }
        }

        // 2. Email extraction
        Matcher emailMatcher = EMAIL_PATTERN.matcher(rawText);
        if (emailMatcher.find()) {
            dto.setEmail(emailMatcher.group());
        }

        // 3. Phone extraction
        Matcher phoneMatcher = PHONE_PATTERN.matcher(rawText);
        if (phoneMatcher.find()) {
            dto.setPhone(phoneMatcher.group());
        }

        // 4. CGPA extraction
        Matcher cgpaMatcher = CGPA_PATTERN.matcher(rawText);
        if (cgpaMatcher.find()) {
            dto.setCgpa(cgpaMatcher.group(1));
        }

        // 5. Degree heuristic
        String lowerText = rawText.toLowerCase();
        for (String deg : DEGREE_KEYWORDS) {
            if (lowerText.contains(deg)) {
                dto.setDegree(deg.toUpperCase());
                break;
            }
        }

        // 6. Year heuristic
        Matcher yearMatcher = GRAD_YEAR_PATTERN.matcher(rawText);
        int latestYear = 0;
        while (yearMatcher.find()) {
            try {
                int y = Integer.parseInt(yearMatcher.group(1));
                if (y >= 2000 && y <= 2030 && y > latestYear) {
                    latestYear = y;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (latestYear > 0) {
            dto.setGraduationYear(latestYear);
        }

        // 7. Extract section snippets (Projects, Experience, Education)
        extractSections(lines, dto);

        return dto;
    }

    private void extractSections(String[] lines, ExtractedDataDto dto) {
        List<String> experience = new ArrayList<>();
        List<String> projects = new ArrayList<>();
        List<String> education = new ArrayList<>();
        List<String> certifications = new ArrayList<>();

        String currentSection = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            if (lower.contains("experience") || lower.contains("work history") || lower.contains("employment")) {
                currentSection = "EXP";
                continue;
            } else if (lower.contains("project") || lower.contains("portfolio")) {
                currentSection = "PROJ";
                continue;
            } else if (lower.contains("education") || lower.contains("academic") || lower.contains("qualification")) {
                currentSection = "EDU";
                continue;
            } else if (lower.contains("certification") || lower.contains("certificate") || lower.contains("achievements")) {
                currentSection = "CERT";
                continue;
            } else if (lower.contains("skills") || lower.contains("technologies")) {
                currentSection = "SKILLS";
                continue;
            }

            if (trimmed.length() > 5) {
                switch (currentSection) {
                    case "EXP":
                        if (experience.size() < 6) experience.add(trimmed);
                        break;
                    case "PROJ":
                        if (projects.size() < 6) projects.add(trimmed);
                        break;
                    case "EDU":
                        if (education.size() < 4) education.add(trimmed);
                        break;
                    case "CERT":
                        if (certifications.size() < 4) certifications.add(trimmed);
                        break;
                }
            }
        }

        dto.setExperience(experience);
        dto.setProjects(projects);
        dto.setEducation(education);
        dto.setCertifications(certifications);
    }
}
