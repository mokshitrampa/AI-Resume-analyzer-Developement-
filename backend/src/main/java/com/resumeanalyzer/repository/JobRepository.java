package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Job;
import com.resumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByActiveTrueOrderByCreatedAtDesc();
    List<Job> findByRecruiterOrderByCreatedAtDesc(User recruiter);
    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Job> searchJobs(@Param("title") String title, @Param("location") String location);
}
