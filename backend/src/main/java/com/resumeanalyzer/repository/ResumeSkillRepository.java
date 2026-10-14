package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {
    List<ResumeSkill> findByResume(Resume resume);
    List<ResumeSkill> findByResumeId(Long resumeId);
    void deleteByResume(Resume resume);
}
