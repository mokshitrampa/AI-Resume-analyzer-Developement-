package com.resumeanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.job.JobCreateRequest;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final ExtractedResumeDataRepository extractedResumeDataRepository;
    private final JobMatchingEngine jobMatchingEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobService(JobRepository jobRepository,
                      SavedJobRepository savedJobRepository,
                      JobApplicationRepository jobApplicationRepository,
                      ResumeRepository resumeRepository,
                      ExtractedResumeDataRepository extractedResumeDataRepository,
                      JobMatchingEngine jobMatchingEngine) {
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.resumeRepository = resumeRepository;
        this.extractedResumeDataRepository = extractedResumeDataRepository;
        this.jobMatchingEngine = jobMatchingEngine;
    }

    @PostConstruct
    public void seedInitialJobs() {
        if (jobRepository.count() == 0) {
            seedSampleJobs();
        }
    }

    public List<JobDto> getAllJobs(String title, String location, Long currentUserId) {
        List<Job> jobs;
        if ((title != null && !title.isBlank()) || (location != null && !location.isBlank())) {
            jobs = jobRepository.searchJobs(title, location);
        } else {
            jobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        }

        Set<Long> savedJobIds = new HashSet<>();
        if (currentUserId != null) {
            List<SavedJob> saved = savedJobRepository.findByUserIdOrderBySavedAtDesc(currentUserId);
            for (SavedJob sj : saved) {
                savedJobIds.add(sj.getJob().getId());
            }
        }

        return jobs.stream().map(j -> toDto(j, savedJobIds.contains(j.getId()), null)).collect(Collectors.toList());
    }

    public JobDto getJobById(Long id, Long currentUserId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        boolean isSaved = currentUserId != null && savedJobRepository.existsByUserIdAndJobId(currentUserId, id);
        return toDto(job, isSaved, null);
    }

    @Transactional
    public void saveJob(Long userId, Long jobId) {
        if (savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            return;
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User user = new User();
        user.setId(userId);
        savedJobRepository.save(new SavedJob(user, job));
    }

    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
    }

    public List<JobDto> getSavedJobs(Long userId) {
        List<SavedJob> savedJobs = savedJobRepository.findByUserIdOrderBySavedAtDesc(userId);
        return savedJobs.stream().map(sj -> toDto(sj.getJob(), true, null)).collect(Collectors.toList());
    }

    @Transactional
    public void applyForJob(Long userId, Long jobId, Long resumeId, String coverLetter) {
        if (jobApplicationRepository.existsByJobIdAndStudentId(jobId, userId)) {
            throw new BadRequestException("You have already submitted an application for this position.");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Resume resume;
        if (resumeId != null) {
            resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        } else {
            resume = resumeRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                    .orElseThrow(() -> new BadRequestException("Please upload a resume first before submitting applications."));
        }

        User student = new User();
        student.setId(userId);

        JobApplication app = new JobApplication(job, student, resume);
        app.setCoverLetter(coverLetter);

        // Calculate score for recruiter view
        ExtractedResumeData extracted = extractedResumeDataRepository.findByResumeId(resume.getId()).orElse(null);
        if (extracted != null) {
            var matchRes = jobMatchingEngine.matchResumeWithJob(resume, mapToExtractedDto(extracted), job);
            app.setMatchScore(matchRes.getOverallScore());
        }

        jobApplicationRepository.save(app);
    }

    public List<JobDto> getRecommendedJobs(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        ExtractedResumeData data = extractedResumeDataRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new BadRequestException("Extracted resume data not found. Please upload or analyze resume first."));

        List<Job> allJobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        List<JobDto> recommendations = new ArrayList<>();
        ExtractedDataDto dataDto = mapToExtractedDto(data);

        for (Job j : allJobs) {
            var match = jobMatchingEngine.matchResumeWithJob(resume, dataDto, j);
            JobDto dto = toDto(j, false, match.getOverallScore());
            recommendations.add(dto);
        }

        // Sort descending by calculated score
        recommendations.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        return recommendations;
    }

    private ExtractedDataDto mapToExtractedDto(ExtractedResumeData data) {
        ExtractedDataDto dto = new ExtractedDataDto();
        dto.setId(data.getId());
        dto.setResumeId(data.getResume().getId());
        dto.setCandidateName(data.getCandidateName());
        dto.setEmail(data.getEmail());
        dto.setPhone(data.getPhone());
        dto.setLocation(data.getLocation());
        dto.setDegree(data.getDegree());
        dto.setGraduationYear(data.getGraduationYear());
        dto.setCgpa(data.getCgpa());
        dto.setRawText(data.getRawText());

        try {
            if (data.getTechnicalSkillsJson() != null)
                dto.setTechnicalSkills(objectMapper.readValue(data.getTechnicalSkillsJson(), new TypeReference<>() {}));
            if (data.getExperienceJson() != null)
                dto.setExperience(objectMapper.readValue(data.getExperienceJson(), new TypeReference<>() {}));
            if (data.getEducationJson() != null)
                dto.setEducation(objectMapper.readValue(data.getEducationJson(), new TypeReference<>() {}));
            if (data.getProjectsJson() != null)
                dto.setProjects(objectMapper.readValue(data.getProjectsJson(), new TypeReference<>() {}));
            if (data.getCertificationsJson() != null)
                dto.setCertifications(objectMapper.readValue(data.getCertificationsJson(), new TypeReference<>() {}));
        } catch (Exception ignored) {}

        return dto;
    }

    private JobDto toDto(Job job, boolean saved, Integer matchScore) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setWorkMode(job.getWorkMode());
        dto.setEmploymentType(job.getEmploymentType());
        dto.setSalaryRange(job.getSalaryRange());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setDescription(job.getDescription());
        dto.setApplicationLink(job.getApplicationLink());
        dto.setActive(job.isActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setSaved(saved);
        dto.setMatchScore(matchScore);

        if (job.getRequiredSkills() != null) {
            dto.setRequiredSkills(Arrays.stream(job.getRequiredSkills().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }
        if (job.getPreferredSkills() != null) {
            dto.setPreferredSkills(Arrays.stream(job.getPreferredSkills().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }

        return dto;
    }

    private void seedSampleJobs() {
        createSeedJob("Java Backend Engineer", "Apex Global Tech", "Bengaluru, India (or Remote)",
                WorkMode.HYBRID, EmploymentType.FULL_TIME, "₹12,00,000 - ₹18,00,000", "0-2 Years",
                "Looking for a passionate Java Developer to build high-performance microservices and RESTful APIs using Spring Boot and MySQL.",
                "Java, Spring Boot, REST API, MySQL, Git", "Docker, Kubernetes, Redis, Microservices");

        createSeedJob("Full Stack Developer", "CloudScale Networks", "Hyderabad, India (Hybrid)",
                WorkMode.HYBRID, EmploymentType.FULL_TIME, "₹10,00,000 - ₹16,00,000", "Entry Level",
                "Join our engineering team to design modern web dashboards in React and backend services in Spring Boot and PostgreSQL.",
                "React, JavaScript, HTML5, CSS3, Java, Spring Boot, SQL", "TypeScript, Tailwind CSS, Docker");

        createSeedJob("Python Backend Developer", "DataSphere Analytics", "Pune, India (Remote)",
                WorkMode.REMOTE, EmploymentType.FULL_TIME, "₹9,00,000 - ₹15,00,000", "1-3 Years",
                "Design scalable data ingestion microservices and REST APIs using FastAPI, Django, and PostgreSQL.",
                "Python, Django, FastAPI, PostgreSQL, Git", "Redis, Docker, AWS, Celery");

        createSeedJob("Frontend React Specialist", "NextGen UI Labs", "Mumbai, India",
                WorkMode.ONSITE, EmploymentType.FULL_TIME, "₹8,00,000 - ₹14,00,000", "0-2 Years",
                "Craft responsive, highly performant web interfaces with modern React, Tailwind CSS, and state management libraries.",
                "React, JavaScript, TypeScript, Tailwind CSS, HTML5, CSS3", "Next.js, Redux, REST API");

        createSeedJob("Cloud & DevOps Engineer", "InfraCore Cloud Solutions", "Gurugram, India",
                WorkMode.HYBRID, EmploymentType.FULL_TIME, "₹14,00,000 - ₹20,00,000", "1-3 Years",
                "Automate infrastructure provisioning, maintain CI/CD pipelines, and deploy containerized services onto Kubernetes.",
                "Docker, Kubernetes, Linux, Git, Jenkins, AWS", "Terraform, Python, Prometheus");

        createSeedJob("Junior Software Engineer", "InnoTech Systems", "Noida, India",
                WorkMode.HYBRID, EmploymentType.FULL_TIME, "₹6,00,000 - ₹10,00,000", "Fresher / 0-1 Year",
                "Great opportunity for recent computer science graduates to work across software design, unit testing, and full lifecycle engineering.",
                "Java, SQL, Git, HTML5, OOP", "Spring Boot, JUnit, REST API");

        createSeedJob("Database Developer & Analyst", "SecureData Matrix", "Chennai, India",
                WorkMode.REMOTE, EmploymentType.FULL_TIME, "₹9,00,000 - ₹13,00,000", "1-2 Years",
                "Optimize queries, model relational schemas, and maintain high data integrity across MySQL and PostgreSQL clusters.",
                "SQL, MySQL, PostgreSQL, Python, Git", "MongoDB, Redis, Linux");
    }

    private void createSeedJob(String title, String company, String loc, WorkMode wm, EmploymentType et,
                               String sal, String exp, String desc, String req, String pref) {
        Job j = new Job();
        j.setTitle(title);
        j.setCompanyName(company);
        j.setLocation(loc);
        j.setWorkMode(wm);
        j.setEmploymentType(et);
        j.setSalaryRange(sal);
        j.setExperienceLevel(exp);
        j.setDescription(desc);
        j.setRequiredSkills(req);
        j.setPreferredSkills(pref);
        j.setActive(true);
        jobRepository.save(j);
    }
}
