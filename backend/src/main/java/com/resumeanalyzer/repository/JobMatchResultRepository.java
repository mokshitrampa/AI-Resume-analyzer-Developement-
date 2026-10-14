package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.JobMatchResult;
import com.resumeanalyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchResultRepository extends JpaRepository<JobMatchResult, Long> {
    Optional<JobMatchResult> findFirstByResumeAndJobOrderByMatchedAtDesc(Resume resume, Job job);
    Optional<JobMatchResult> findFirstByResumeIdAndJobIdOrderByMatchedAtDesc(Long resumeId, Long jobId);
    List<JobMatchResult> findByResumeUserIdOrderByMatchedAtDesc(Long userId);
    List<JobMatchResult> findByResumeIdOrderByOverallScoreDesc(Long resumeId);
}
