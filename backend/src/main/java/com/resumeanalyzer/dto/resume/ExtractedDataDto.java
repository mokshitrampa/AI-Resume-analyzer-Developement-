package com.resumeanalyzer.dto.resume;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExtractedDataDto {

    private Long id;
    private Long resumeId;
    private String candidateName;
    private String email;
    private String phone;
    private String location;
    private String degree;
    private String institution;
    private Integer graduationYear;
    private String cgpa;
    private String rawText;
    private List<String> technicalSkills = new ArrayList<>();
    private List<String> experience = new ArrayList<>();
    private List<String> education = new ArrayList<>();
    private List<String> projects = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private LocalDateTime extractedAt;

    public ExtractedDataDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

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

    public List<String> getTechnicalSkills() { return technicalSkills; }
    public void setTechnicalSkills(List<String> technicalSkills) { this.technicalSkills = technicalSkills; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public LocalDateTime getExtractedAt() { return extractedAt; }
    public void setExtractedAt(LocalDateTime extractedAt) { this.extractedAt = extractedAt; }
}
