package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.job.JobCreateRequest;
import com.resumeanalyzer.dto.job.JobDto;
import com.resumeanalyzer.dto.recruiter.ApplicationDto;
import com.resumeanalyzer.dto.recruiter.RecruiterStatsDto;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedException;
import com.resumeanalyzer.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecruiterService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public RecruiterService(JobRepository jobRepository,
                            JobApplicationRepository jobApplicationRepository,
                            UserRepository userRepository,
                            RecruiterProfileRepository recruiterProfileRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterStatsDto getDashboardStats(Long recruiterId) {
        List<Job> myJobs = jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId);
        long totalJobs = myJobs.size();
        long activeJobs = myJobs.stream().filter(Job::isActive).count();

        long totalApps = 0;
        long shortlisted = 0;
        for (Job j : myJobs) {
            List<JobApplication> apps = jobApplicationRepository.findByJob(j);
            totalApps += apps.size();
            shortlisted += apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        }

        return new RecruiterStatsDto(totalJobs, activeJobs, totalApps, shortlisted);
    }

    @Transactional(readOnly = true)
    public List<JobDto> getMyJobs(Long recruiterId) {
        List<Job> myJobs = jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId);
        return myJobs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public JobDto createJob(Long recruiterId, JobCreateRequest req) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setTitle(req.getTitle());
        job.setCompanyName(req.getCompanyName());
        job.setLocation(req.getLocation());
        job.setWorkMode(req.getWorkMode());
        job.setEmploymentType(req.getEmploymentType());
        job.setSalaryRange(req.getSalaryRange());
        job.setExperienceLevel(req.getExperienceLevel());
        job.setDescription(req.getDescription());
        job.setRequiredSkills(req.getRequiredSkills());
        job.setPreferredSkills(req.getPreferredSkills());
        job.setApplicationLink(req.getApplicationLink());
        job.setActive(true);

        Job saved = jobRepository.save(job);
        return toDto(saved);
    }

    @Transactional
    public JobDto updateJob(Long recruiterId, Long jobId, JobCreateRequest req) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getRecruiter() != null && !job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not have permission to edit this job posting.");
        }

        job.setTitle(req.getTitle());
        job.setCompanyName(req.getCompanyName());
        job.setLocation(req.getLocation());
        job.setWorkMode(req.getWorkMode());
        job.setEmploymentType(req.getEmploymentType());
        job.setSalaryRange(req.getSalaryRange());
        job.setExperienceLevel(req.getExperienceLevel());
        job.setDescription(req.getDescription());
        job.setRequiredSkills(req.getRequiredSkills());
        job.setPreferredSkills(req.getPreferredSkills());
        job.setApplicationLink(req.getApplicationLink());

        return toDto(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long recruiterId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getRecruiter() != null && !job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not have permission to delete this job posting.");
        }

        jobRepository.delete(job);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getJobApplications(Long recruiterId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getRecruiter() != null && !job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("Access denied to applications for this job.");
        }

        List<JobApplication> apps = jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
        return apps.stream().map(this::toAppDto).collect(Collectors.toList());
    }

    @Transactional
    public ApplicationDto updateApplicationStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String notes) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (app.getJob().getRecruiter() != null && !app.getJob().getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You cannot update applications for jobs you did not author.");
        }

        app.setStatus(newStatus);
        if (notes != null) {
            app.setRecruiterNotes(notes);
        }

        JobApplication saved = jobApplicationRepository.save(app);
        return toAppDto(saved);
    }

    private JobDto toDto(Job job) {
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
        dto.setActive(job.isActive());
        dto.setCreatedAt(job.getCreatedAt());

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

    private ApplicationDto toAppDto(JobApplication app) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setJobId(app.getJob().getId());
        dto.setJobTitle(app.getJob().getTitle());
        dto.setStudentId(app.getStudent().getId());
        dto.setStudentName(app.getStudent().getFullName());
        dto.setStudentEmail(app.getStudent().getEmail());
        dto.setStudentPhone(app.getStudent().getPhone());
        dto.setResumeId(app.getResume().getId());
        dto.setResumeFilename(app.getResume().getOriginalFilename());
        dto.setMatchScore(app.getMatchScore());
        dto.setStatus(app.getStatus());
        dto.setCoverLetter(app.getCoverLetter());
        dto.setRecruiterNotes(app.getRecruiterNotes());
        dto.setAppliedAt(app.getAppliedAt());
        return dto;
    }
}
