package com.resumeanalyzer.service;

import java.util.*;
import java.util.regex.Pattern;

public class SkillExtractionService {

    private final Map<String, List<String>> skillSynonyms = new LinkedHashMap<>();

    public SkillExtractionService() {
        seedDictionary();
    }

    private void seedDictionary() {
        add("Java", "java 21", "java 17", "java 11", "java 8", "core java", "jse", "jdk");
        add("Python", "py", "python3", "python 3");
        add("JavaScript", "js", "ecmascript", "es6", "vanilla js");
        add("TypeScript", "ts");
        add("C++", "cpp", "c plus plus");
        add("C#", "csharp", "c sharp", ".net");
        add("Go", "golang");
        add("Rust", "rustlang");
        add("Kotlin", "kt");
        add("SQL", "structured query language");

        add("Spring Boot", "springboot", "spring-boot", "spring framework", "spring");
        add("Hibernate", "hibernate jpa", "jpa", "spring data jpa");
        add("React", "react.js", "reactjs");
        add("Angular", "angular.js", "angularjs");
        add("Vue.js", "vue", "vuejs");
        add("Node.js", "nodejs", "node");
        add("Express.js", "express", "expressjs");
        add("Django", "django framework");
        add("FastAPI", "fast api");
        add("Flask");
        add("Tailwind CSS", "tailwindcss", "tailwind");
        add("Bootstrap", "bootstrap 5");
        add("Redux", "redux toolkit");

        add("MySQL", "my-sql");
        add("PostgreSQL", "postgres", "pgsql");
        add("MongoDB", "mongo");
        add("Redis", "redis cache");
        add("Oracle Database", "oracle db", "oracle sql");
        add("SQLite", "sqlite3");
        add("Elasticsearch", "elastic search", "elk");

        add("Docker", "docker container", "containerization");
        add("Kubernetes", "k8s");
        add("AWS", "amazon web services", "ec2", "s3", "lambda");
        add("Google Cloud", "gcp", "google cloud platform");
        add("Microsoft Azure", "azure");
        add("Git", "github", "gitlab", "version control");
        add("CI/CD", "continuous integration", "jenkins", "github actions");
        add("Linux", "ubuntu", "debian", "centos", "redhat");

        add("REST API", "restful api", "rest web services", "restful", "rest");
        add("GraphQL", "gql");
        add("Microservices", "microservice architecture", "distributed systems");
        add("Kafka", "apache kafka");
        add("RabbitMQ", "rabbitmq queue");
        add("Unit Testing", "junit", "mockito", "jest", "pytest");
        add("HTML5", "html");
        add("CSS3", "css");
    }

    private void add(String canonical, String... aliases) {
        List<String> list = new ArrayList<>();
        list.add(canonical.toLowerCase());
        for (String a : aliases) {
            list.add(a.toLowerCase());
        }
        skillSynonyms.put(canonical, list);
    }

    public List<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Clean text: keep internal dots, strip punctuation
        String normalized = text.toLowerCase()
                .replaceAll("\\.(?=\\s|$)", " ")
                .replaceAll("[,/|()\\[\\]{}:;!]", " ");

        Set<String> detected = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : skillSynonyms.entrySet()) {
            String canonical = entry.getKey();
            for (String synonym : entry.getValue()) {
                String pattern = "\\b" + Pattern.quote(synonym) + "\\b";
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(normalized).find()) {
                    detected.add(canonical);
                    break;
                }
            }
        }

        return new ArrayList<>(detected);
    }
}
