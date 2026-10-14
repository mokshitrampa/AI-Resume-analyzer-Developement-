package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.ExtractedResumeData;
import com.resumeanalyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtractedResumeDataRepository extends JpaRepository<ExtractedResumeData, Long> {
    Optional<ExtractedResumeData> findByResume(Resume resume);
    Optional<ExtractedResumeData> findByResumeId(Long resumeId);
}
