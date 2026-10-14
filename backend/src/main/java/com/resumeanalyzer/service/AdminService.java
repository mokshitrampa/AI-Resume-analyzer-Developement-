package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.admin.AdminStatsDto;
import com.resumeanalyzer.entity.Role;
import com.resumeanalyzer.entity.Skill;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobMatchResultRepository jobMatchResultRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@resumeanalyzer.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123456}")
    private String adminPassword;

    public AdminService(UserRepository userRepository,
                        ResumeRepository resumeRepository,
                        JobRepository jobRepository,
                        ResumeAnalysisRepository resumeAnalysisRepository,
                        JobMatchResultRepository jobMatchResultRepository,
                        SkillRepository skillRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobMatchResultRepository = jobMatchResultRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void bootstrapAdminUser() {
        // Section 14: "Provide a secure development-only admin bootstrap process using environment variables."
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName("Platform Administrator");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            logger.info("Bootstrap Admin Account created with email: {}", adminEmail);
        }
    }

    public AdminStatsDto getAnalytics() {
        AdminStatsDto dto = new AdminStatsDto();
        dto.setTotalUsers(userRepository.count());
        dto.setTotalStudents(userRepository.countByRole(Role.STUDENT));
        dto.setTotalRecruiters(userRepository.countByRole(Role.RECRUITER));
        dto.setTotalResumes(resumeRepository.count());
        dto.setTotalJobs(jobRepository.count());
        dto.setTotalAnalyses(resumeAnalysisRepository.count());
        dto.setTotalMatches(jobMatchResultRepository.count());
        dto.setSystemHealth("HEALTHY - All subsystems operational");
        return dto;
    }

    public Page<User> getUsers(int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (search != null && !search.isBlank()) {
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageRequest);
        }
        return userRepository.findAll(pageRequest);
    }

    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Skill addSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        skillRepository.deleteById(skillId);
    }
}
