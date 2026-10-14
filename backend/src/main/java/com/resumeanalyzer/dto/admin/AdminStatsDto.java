package com.resumeanalyzer.dto.admin;

public class AdminStatsDto {

    private long totalUsers;
    private long totalStudents;
    private long totalRecruiters;
    private long totalResumes;
    private long totalJobs;
    private long totalAnalyses;
    private long totalMatches;
    private String systemHealth = "UP";

    public AdminStatsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalRecruiters() { return totalRecruiters; }
    public void setTotalRecruiters(long totalRecruiters) { this.totalRecruiters = totalRecruiters; }

    public long getTotalResumes() { return totalResumes; }
    public void setTotalResumes(long totalResumes) { this.totalResumes = totalResumes; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getTotalAnalyses() { return totalAnalyses; }
    public void setTotalAnalyses(long totalAnalyses) { this.totalAnalyses = totalAnalyses; }

    public long getTotalMatches() { return totalMatches; }
    public void setTotalMatches(long totalMatches) { this.totalMatches = totalMatches; }

    public String getSystemHealth() { return systemHealth; }
    public void setSystemHealth(String systemHealth) { this.systemHealth = systemHealth; }
}
