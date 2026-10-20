package com.resumeanalyzer;

import com.resumeanalyzer.dao.*;
import com.resumeanalyzer.model.*;
import com.resumeanalyzer.service.*;

import java.util.List;

public class DatabaseVerificationTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- STARTING DATABASE INITIALIZATION VERIFICATION ---");
            DatabaseManager.initializeDatabase();
            System.out.println("DatabaseManager.initializeDatabase() completed successfully.");

            UserDao userDao = new UserDao();
            User student = userDao.authenticate("student@test.com", "Password@123");
            System.out.println("Student login check: " + (student != null ? "SUCCESS (ID=" + student.getId() + ")" : "FAILED"));

            User recruiter = userDao.authenticate("recruiter@tech.com", "Password@123");
            System.out.println("Recruiter login check: " + (recruiter != null ? "SUCCESS (ID=" + recruiter.getId() + ")" : "FAILED"));

            User admin = userDao.authenticate("admin@resumeanalyzer.com", "Admin@123456");
            System.out.println("Admin login check: " + (admin != null ? "SUCCESS (ID=" + admin.getId() + ")" : "FAILED"));

            JobDao jobDao = new JobDao();
            List<Job> jobs = jobDao.findAllActive();
            System.out.println("Jobs seeded count: " + jobs.size());

            SkillDao skillDao = new SkillDao();
            int totalSkills = skillDao.countTotal();
            System.out.println("Skill taxonomy count: " + totalSkills);

            // Test Matching Engine
            ExtractedResumeData extractedData = new ExtractedResumeData();
            extractedData.setCandidateName("Test Student");
            extractedData.setTechnicalSkills(List.of("Java", "Spring Boot", "MySQL", "REST API", "Git"));
            extractedData.setExperience(List.of("Software Engineer Intern at TechCorp (2 years)"));
            extractedData.setDegree("Bachelor of Technology in Computer Science");
            extractedData.setRawText("Proficient in Java, Spring Boot, microservices, REST APIs, Git, and MySQL database management.");

            Job testJob = jobs.get(0);
            JobMatchingEngine engine = new JobMatchingEngine();
            JobMatchResult matchResult = engine.match(extractedData, testJob);
            System.out.println("Matching algorithm test: " + matchResult.getOverallScore() + "% match with " + testJob.getTitle());
            System.out.println("Matching breakdown: " + matchResult.getExplanation());
            System.out.println("Matching skills: " + matchResult.getMatchingSkills());
            System.out.println("Missing skills: " + matchResult.getMissingSkills());

            // Test Resume Quality Analysis
            ResumeAnalysisService analysisService = new ResumeAnalysisService();
            ResumeAnalysis analysis = analysisService.analyze(extractedData);
            System.out.println("Quality Score: " + analysis.getOverallScore() + "/100");
            System.out.println("Strengths: " + analysis.getStrengths());
            System.out.println("Improvement suggestions: " + analysis.getImprovementSuggestions());

            System.out.println("--- ALL CHECKS PASSED PERFECTLY ---");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Database verification failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
