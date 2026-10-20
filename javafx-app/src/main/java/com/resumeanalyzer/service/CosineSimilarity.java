package com.resumeanalyzer.service;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CosineSimilarity {

    public double calculate(Map<String, Double> vecA, Map<String, Double> vecB) {
        if (vecA == null || vecB == null || vecA.isEmpty() || vecB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(vecA.keySet());
        intersection.retainAll(vecB.keySet());

        double dotProduct = 0.0;
        for (String term : intersection) {
            dotProduct += vecA.get(term) * vecB.get(term);
        }

        double normA = 0.0;
        for (double val : vecA.values()) {
            normA += val * val;
        }

        double normB = 0.0;
        for (double val : vecB.values()) {
            normB += val * val;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / denominator;
        return Math.max(0.0, Math.min(1.0, similarity));
    }
}
