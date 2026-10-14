package com.resumeanalyzer.dto.matching;

public class ComponentScoreDto {

    private String name;
    private int rawScore;
    private double effectiveWeightPercentage;
    private double contributedPoints;

    public ComponentScoreDto() {}

    public ComponentScoreDto(String name, int rawScore, double effectiveWeightPercentage, double contributedPoints) {
        this.name = name;
        this.rawScore = rawScore;
        this.effectiveWeightPercentage = effectiveWeightPercentage;
        this.contributedPoints = contributedPoints;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getRawScore() { return rawScore; }
    public void setRawScore(int rawScore) { this.rawScore = rawScore; }

    public double getEffectiveWeightPercentage() { return effectiveWeightPercentage; }
    public void setEffectiveWeightPercentage(double effectiveWeightPercentage) { this.effectiveWeightPercentage = effectiveWeightPercentage; }

    public double getContributedPoints() { return contributedPoints; }
    public void setContributedPoints(double contributedPoints) { this.contributedPoints = contributedPoints; }
}
