package com.resumeanalyzer.dto.matching;

import jakarta.validation.constraints.NotNull;

public class JobMatchRequestDto {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    public JobMatchRequestDto() {}

    public JobMatchRequestDto(Long resumeId, Long jobId) {
        this.resumeId = resumeId;
        this.jobId = jobId;
    }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
}
