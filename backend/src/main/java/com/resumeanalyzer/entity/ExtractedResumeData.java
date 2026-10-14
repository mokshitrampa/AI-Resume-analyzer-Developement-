package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "extracted_resume_data")
public class ExtractedResumeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Column(length = 100)
    private String candidateName;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String degree;

    @Column(length = 150)
    private String institution;

    private Integer graduationYear;

    @Column(length = 10)
    private String cgpa;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String technicalSkillsJson; // JSON array of detected skills

    @Lob
    @Column(columnDefinition = "TEXT")
    private String experienceJson; // JSON array of experience descriptions

    @Lob
    @Column(columnDefinition = "TEXT")
    private String educationJson; // JSON array of education details

    @Lob
    @Column(columnDefinition = "TEXT")
    private String projectsJson; // JSON array of project summaries

    @Lob
    @Column(columnDefinition = "TEXT")
    private String certificationsJson; // JSON array of certifications

    @Column(nullable = false)
    private LocalDateTime extractedAt;

    public ExtractedResumeData() {}

    public ExtractedResumeData(Resume resume) {
        this.resume = resume;
    }

    @PrePersist
    protected void onCreate() {
        this.extractedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getTechnicalSkillsJson() { return technicalSkillsJson; }
    public void setTechnicalSkillsJson(String technicalSkillsJson) { this.technicalSkillsJson = technicalSkillsJson; }

    public String getExperienceJson() { return experienceJson; }
    public void setExperienceJson(String experienceJson) { this.experienceJson = experienceJson; }

    public String getEducationJson() { return educationJson; }
    public void setEducationJson(String educationJson) { this.educationJson = educationJson; }

    public String getProjectsJson() { return projectsJson; }
    public void setProjectsJson(String projectsJson) { this.projectsJson = projectsJson; }

    public String getCertificationsJson() { return certificationsJson; }
    public void setCertificationsJson(String certificationsJson) { this.certificationsJson = certificationsJson; }

    public LocalDateTime getExtractedAt() { return extractedAt; }
}
