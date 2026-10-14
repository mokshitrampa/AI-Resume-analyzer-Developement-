package com.resumeanalyzer.algorithm;

import java.util.ArrayList;
import java.util.List;

public class WeightedScoreCalculator {

    public static class ComponentScore {
        private final String name;
        private final int rawScore; // 0 to 100
        private final double configuredWeight; // e.g. 0.40
        private final boolean applicable;
        private double effectiveWeight; // redistributed weight
        private double contributedScore;

        public ComponentScore(String name, int rawScore, double configuredWeight, boolean applicable) {
            this.name = name;
            this.rawScore = Math.max(0, Math.min(100, rawScore));
            this.configuredWeight = configuredWeight;
            this.applicable = applicable;
        }

        public String getName() { return name; }
        public int rawScore() { return rawScore; }
        public double getConfiguredWeight() { return configuredWeight; }
        public boolean isApplicable() { return applicable; }
        public double getEffectiveWeight() { return effectiveWeight; }
        public void setEffectiveWeight(double effectiveWeight) { this.effectiveWeight = effectiveWeight; }
        public double getContributedScore() { return contributedScore; }
        public void setContributedScore(double contributedScore) { this.contributedScore = contributedScore; }
    }

    public static class CalculationResult {
        private final int overallScore;
        private final List<ComponentScore> components;
        private final String explanation;

        public CalculationResult(int overallScore, List<ComponentScore> components, String explanation) {
            this.overallScore = overallScore;
            this.components = components;
            this.explanation = explanation;
        }

        public int getOverallScore() { return overallScore; }
        public List<ComponentScore> getComponents() { return components; }
        public String getExplanation() { return explanation; }
    }

    public static CalculationResult calculate(
            int reqSkillScore, boolean hasReqSkills,
            int prefSkillScore, boolean hasPrefSkills,
            int semanticScore,
            int experienceScore, boolean hasExpReq,
            int educationScore, boolean hasEduReq) {

        List<ComponentScore> components = new ArrayList<>();
        components.add(new ComponentScore("Required Skills", reqSkillScore, 0.40, hasReqSkills));
        components.add(new ComponentScore("Preferred Skills", prefSkillScore, 0.20, hasPrefSkills));
        components.add(new ComponentScore("Semantic / TF-IDF Similarity", semanticScore, 0.20, true));
        components.add(new ComponentScore("Experience Match", experienceScore, 0.10, hasExpReq));
        components.add(new ComponentScore("Education & Qualification", educationScore, 0.10, hasEduReq));

        // Sum applicable weights
        double totalApplicableWeight = 0.0;
        for (ComponentScore c : components) {
            if (c.isApplicable()) {
                totalApplicableWeight += c.getConfiguredWeight();
            }
        }

        if (totalApplicableWeight <= 0.0) {
            totalApplicableWeight = 1.0;
        }

        double totalScoreAccumulator = 0.0;
        StringBuilder explanation = new StringBuilder("Job Match Score Calculation Breakdown:\n");

        for (ComponentScore c : components) {
            if (c.isApplicable()) {
                // Redistribute weights proportionally
                double normalizedWeight = c.getConfiguredWeight() / totalApplicableWeight;
                c.setEffectiveWeight(normalizedWeight);
                double contribution = c.rawScore() * normalizedWeight;
                c.setContributedScore(contribution);
                totalScoreAccumulator += contribution;

                explanation.append(String.format("- %s: Score %d%% × Effective Weight %.1f%% = +%.1f pts\n",
                        c.getName(), c.rawScore(), normalizedWeight * 100.0, contribution));
            } else {
                c.setEffectiveWeight(0.0);
                c.setContributedScore(0.0);
                explanation.append(String.format("- %s: Not required by job description (weight redistributed)\n", c.getName()));
            }
        }

        int finalScore = (int) Math.round(Math.max(0, Math.min(100, totalScoreAccumulator)));
        explanation.append(String.format("Final Calculated Score: %d / 100", finalScore));

        return new CalculationResult(finalScore, components, explanation.toString());
    }
}
