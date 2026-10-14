package com.resumeanalyzer.service;

import com.resumeanalyzer.entity.Skill;
import com.resumeanalyzer.entity.SkillCategory;
import com.resumeanalyzer.repository.SkillRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SkillExtractionService {

    private final SkillRepository skillRepository;

    // In-memory alias dictionary mapping lowercased variation/synonym -> canonical skill name
    private final Map<String, String> aliasToCanonicalMap = new HashMap<>();
    private final Map<String, SkillCategory> canonicalToCategoryMap = new HashMap<>();

    public SkillExtractionService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @PostConstruct
    public void initSkillDictionary() {
        // Pre-seed comprehensive dictionary
        registerDefaultSkills();

        // Load existing custom skills from database
        try {
            List<Skill> dbSkills = skillRepository.findAll();
            for (Skill s : dbSkills) {
                registerSkill(s.getName(), s.getCategory(), s.getSynonyms() != null ? Arrays.asList(s.getSynonyms().split(",")) : Collections.emptyList());
            }
        } catch (Exception ignored) {}
    }

    public List<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        Set<String> matchedCanonicalSkills = new LinkedHashSet<>();
        // Strip trailing sentence periods and delimiters while preserving internal dots like node.js and vue.js
        String cleaned = text.toLowerCase()
                .replaceAll("\\.(?=\\s|$)", " ")
                .replaceAll("[,;:!?()\\[\\]{}\"'\\\\/]", " ");
        String normalizedText = " " + cleaned + " ";

        // Match against known skills and aliases
        for (Map.Entry<String, String> entry : aliasToCanonicalMap.entrySet()) {
            String alias = entry.getKey();
            String canonical = entry.getValue();

            // Word-boundary matching
            String regex = "(?i)(?:^|\\s)" + Pattern.quote(alias) + "(?:\\s|$)";
            if (Pattern.compile(regex).matcher(normalizedText).find()) {
                matchedCanonicalSkills.add(canonical);
            }
        }

        return new ArrayList<>(matchedCanonicalSkills);
    }

    public String getCanonicalName(String skillName) {
        if (skillName == null) return "";
        String lower = skillName.trim().toLowerCase();
        return aliasToCanonicalMap.getOrDefault(lower, skillName.trim());
    }

    public SkillCategory getCategory(String canonicalSkill) {
        return canonicalToCategoryMap.getOrDefault(canonicalSkill, SkillCategory.OTHER);
    }

    private void registerSkill(String canonical, SkillCategory category, List<String> aliases) {
        canonicalToCategoryMap.put(canonical, category);
        aliasToCanonicalMap.put(canonical.toLowerCase(), canonical);

        for (String alias : aliases) {
            if (alias != null && !alias.trim().isEmpty()) {
                aliasToCanonicalMap.put(alias.trim().toLowerCase(), canonical);
            }
        }
    }

    private void registerDefaultSkills() {
        // Languages
        registerSkill("Java", SkillCategory.LANGUAGE, Arrays.asList("java 21", "java 17", "java 11", "java 8", "core java", "j2se", "j2ee"));
        registerSkill("Python", SkillCategory.LANGUAGE, Arrays.asList("python3", "py"));
        registerSkill("JavaScript", SkillCategory.LANGUAGE, Arrays.asList("js", "es6", "ecmascript"));
        registerSkill("TypeScript", SkillCategory.LANGUAGE, Arrays.asList("ts"));
        registerSkill("C++", SkillCategory.LANGUAGE, Arrays.asList("cpp"));
        registerSkill("C#", SkillCategory.LANGUAGE, Arrays.asList("csharp", ".net c#"));
        registerSkill("SQL", SkillCategory.LANGUAGE, Arrays.asList("pl/sql", "tsql", "ansi sql"));
        registerSkill("HTML5", SkillCategory.LANGUAGE, Arrays.asList("html"));
        registerSkill("CSS3", SkillCategory.LANGUAGE, Arrays.asList("css"));
        registerSkill("Go", SkillCategory.LANGUAGE, Arrays.asList("golang"));
        registerSkill("Rust", SkillCategory.LANGUAGE, Collections.emptyList());
        registerSkill("Kotlin", SkillCategory.LANGUAGE, Collections.emptyList());
        registerSkill("PHP", SkillCategory.LANGUAGE, Collections.emptyList());

        // Frameworks
        registerSkill("Spring Boot", SkillCategory.FRAMEWORK, Arrays.asList("springboot", "spring boot 3", "spring"));
        registerSkill("Spring MVC", SkillCategory.FRAMEWORK, Arrays.asList("springmvc"));
        registerSkill("Hibernate", SkillCategory.FRAMEWORK, Arrays.asList("jpa", "spring data jpa"));
        registerSkill("React", SkillCategory.FRAMEWORK, Arrays.asList("react.js", "reactjs"));
        registerSkill("Angular", SkillCategory.FRAMEWORK, Arrays.asList("angularjs", "angular 2+"));
        registerSkill("Vue.js", SkillCategory.FRAMEWORK, Arrays.asList("vue", "vuejs"));
        registerSkill("Node.js", SkillCategory.FRAMEWORK, Arrays.asList("nodejs", "node"));
        registerSkill("Express.js", SkillCategory.FRAMEWORK, Arrays.asList("express", "expressjs"));
        registerSkill("Django", SkillCategory.FRAMEWORK, Collections.emptyList());
        registerSkill("Flask", SkillCategory.FRAMEWORK, Collections.emptyList());
        registerSkill("FastAPI", SkillCategory.FRAMEWORK, Collections.emptyList());
        registerSkill("Tailwind CSS", SkillCategory.FRAMEWORK, Arrays.asList("tailwind", "tailwindcss"));
        registerSkill("Bootstrap", SkillCategory.FRAMEWORK, Collections.emptyList());
        registerSkill("Next.js", SkillCategory.FRAMEWORK, Arrays.asList("nextjs"));

        // Databases
        registerSkill("MySQL", SkillCategory.DATABASE, Arrays.asList("mysql 8", "mariadb"));
        registerSkill("PostgreSQL", SkillCategory.DATABASE, Arrays.asList("postgres", "psql"));
        registerSkill("MongoDB", SkillCategory.DATABASE, Arrays.asList("mongo"));
        registerSkill("Redis", SkillCategory.DATABASE, Collections.emptyList());
        registerSkill("Oracle Database", SkillCategory.DATABASE, Arrays.asList("oracle", "oracle db"));
        registerSkill("SQLite", SkillCategory.DATABASE, Collections.emptyList());

        // Cloud & DevOps
        registerSkill("AWS", SkillCategory.CLOUD, Arrays.asList("amazon web services", "ec2", "s3", "lambda"));
        registerSkill("Microsoft Azure", SkillCategory.CLOUD, Arrays.asList("azure"));
        registerSkill("Google Cloud", SkillCategory.CLOUD, Arrays.asList("gcp", "google cloud platform"));
        registerSkill("Docker", SkillCategory.CLOUD, Arrays.asList("containerization", "containers"));
        registerSkill("Kubernetes", SkillCategory.CLOUD, Arrays.asList("k8s"));
        registerSkill("Git", SkillCategory.TOOL, Arrays.asList("github", "gitlab", "version control"));
        registerSkill("Jenkins", SkillCategory.TOOL, Arrays.asList("ci/cd", "ci cd"));
        registerSkill("Linux", SkillCategory.TOOL, Arrays.asList("unix", "bash", "shell scripting"));

        // Concepts & Tools
        registerSkill("REST API", SkillCategory.TOOL, Arrays.asList("restful", "rest apis", "rest web services", "api development"));
        registerSkill("GraphQL", SkillCategory.TOOL, Collections.emptyList());
        registerSkill("Microservices", SkillCategory.METHODOLOGY, Arrays.asList("microservice architecture"));
        registerSkill("JUnit", SkillCategory.TOOL, Arrays.asList("unit testing", "mockito", "testing"));
        registerSkill("Kafka", SkillCategory.TOOL, Arrays.asList("apache kafka"));
        registerSkill("Agile", SkillCategory.METHODOLOGY, Arrays.asList("scrum", "kanban"));
    }
}
