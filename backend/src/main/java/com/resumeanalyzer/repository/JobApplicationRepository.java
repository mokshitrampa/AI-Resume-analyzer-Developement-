package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.JobApplication;
import com.resumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJob(Job job);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"job", "student", "resume"})
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<JobApplication> findByStudent(User student);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"job", "student", "resume"})
    List<JobApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);
    Optional<JobApplication> findByJobIdAndStudentId(Long jobId, Long studentId);
    boolean existsByJobIdAndStudentId(Long jobId, Long studentId);
    long countByJobRecruiterId(Long recruiterId);
}
