package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Skill;
import com.resumeanalyzer.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    List<Skill> findByCategory(SkillCategory category);
    boolean existsByNameIgnoreCase(String name);
}
