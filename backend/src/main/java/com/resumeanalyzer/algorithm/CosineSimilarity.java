package com.resumeanalyzer.algorithm;

public class CosineSimilarity {

    /**
     * Calculates cosine similarity between two double vectors.
     * Returns a normalized value between 0.0 and 1.0 (or 0 to 100 percentage).
     */
    public static double calculate(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length == 0 || vecB.length == 0 || vecA.length != vecB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        // Clamp numerical precision between 0.0 and 1.0
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    public static int calculatePercentage(String docA, String docB) {
        if (docA == null || docB == null || docA.isBlank() || docB.isBlank()) {
            return 0;
        }
        TfIdfVectorizer.VectorPair pair = TfIdfVectorizer.vectorize(docA, docB);
        double sim = calculate(pair.getVectorA(), pair.getVectorB());
        return (int) Math.round(sim * 100.0);
    }
}
