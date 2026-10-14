package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String currentDegree;

    @Column(length = 150)
    private String institution;

    private Integer graduationYear;

    @Column(length = 10)
    private String cgpa;

    @Column(length = 100)
    private String targetJobTitle;

    private int resumeCount = 0;

    public StudentProfile() {}

    public StudentProfile(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCurrentDegree() { return currentDegree; }
    public void setCurrentDegree(String currentDegree) { this.currentDegree = currentDegree; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }

    public String getTargetJobTitle() { return targetJobTitle; }
    public void setTargetJobTitle(String targetJobTitle) { this.targetJobTitle = targetJobTitle; }

    public int getResumeCount() { return resumeCount; }
    public void setResumeCount(int resumeCount) { this.resumeCount = resumeCount; }
}
