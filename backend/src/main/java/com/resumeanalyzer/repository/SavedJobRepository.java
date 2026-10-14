package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.SavedJob;
import com.resumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserOrderBySavedAtDesc(User user);
    List<SavedJob> findByUserIdOrderBySavedAtDesc(Long userId);
    Optional<SavedJob> findByUserAndJob(User user, Job job);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    void deleteByUserIdAndJobId(Long userId, Long jobId);
}
