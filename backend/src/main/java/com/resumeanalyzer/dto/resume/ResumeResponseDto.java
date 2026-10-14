package com.resumeanalyzer.dto.resume;

import java.time.LocalDateTime;

public class ResumeResponseDto {

    private Long id;
    private String originalFilename;
    private String fileType;
    private Long fileSize;
    private int version;
    private LocalDateTime createdAt;
    private Integer qualityScore;

    public ResumeResponseDto() {}

    public ResumeResponseDto(Long id, String originalFilename, String fileType, Long fileSize, int version, LocalDateTime createdAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.version = version;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
}
