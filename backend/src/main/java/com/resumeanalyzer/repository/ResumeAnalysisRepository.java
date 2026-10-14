package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findFirstByResumeOrderByAnalyzedAtDesc(Resume resume);
    Optional<ResumeAnalysis> findFirstByResumeIdOrderByAnalyzedAtDesc(Long resumeId);
    List<ResumeAnalysis> findByResumeUserIdOrderByAnalyzedAtDesc(Long userId);
}
