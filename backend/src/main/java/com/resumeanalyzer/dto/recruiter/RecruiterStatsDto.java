package com.resumeanalyzer.dto.recruiter;

public class RecruiterStatsDto {

    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long shortlistedCount;

    public RecruiterStatsDto() {}

    public RecruiterStatsDto(long totalJobs, long activeJobs, long totalApplications, long shortlistedCount) {
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.totalApplications = totalApplications;
        this.shortlistedCount = shortlistedCount;
    }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(long shortlistedCount) { this.shortlistedCount = shortlistedCount; }
}
