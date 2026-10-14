package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
    List<Resume> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Resume> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUser(User user);
}
