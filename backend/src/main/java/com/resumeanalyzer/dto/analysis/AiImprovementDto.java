package com.resumeanalyzer.dto.analysis;

public class AiImprovementDto {

    private String originalText;
    private String suggestedText;
    private String reason;
    private String category; // ACTION_VERB, IMPACT, TECHNICAL_KEYWORD, CLARITY
    private boolean isAiGenerated; // true = LLM API, false = Rule-based deterministic engine

    public AiImprovementDto() {}

    public AiImprovementDto(String originalText, String suggestedText, String reason, String category, boolean isAiGenerated) {
        this.originalText = originalText;
        this.suggestedText = suggestedText;
        this.reason = reason;
        this.category = category;
        this.isAiGenerated = isAiGenerated;
    }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getSuggestedText() { return suggestedText; }
    public void setSuggestedText(String suggestedText) { this.suggestedText = suggestedText; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAiGenerated() { return isAiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { isAiGenerated = aiGenerated; }
}
