package com.resumeanalyzer.algorithm;

import java.util.*;

public class TfIdfVectorizer {

    /**
     * Vectorizes two documents into aligned TF-IDF vectors.
     */
    public static class VectorPair {
        private final double[] vectorA;
        private final double[] vectorB;

        public VectorPair(double[] vectorA, double[] vectorB) {
            this.vectorA = vectorA;
            this.vectorB = vectorB;
        }

        public double[] getVectorA() { return vectorA; }
        public double[] getVectorB() { return vectorB; }
    }

    public static VectorPair vectorize(String docA, String docB) {
        List<String> tokensA = TextTokenizer.tokenize(docA);
        List<String> tokensB = TextTokenizer.tokenize(docB);

        Map<String, Integer> tfA = TextTokenizer.getTermFrequencies(tokensA);
        Map<String, Integer> tfB = TextTokenizer.getTermFrequencies(tokensB);

        // Build unified vocabulary
        Set<String> vocabulary = new TreeSet<>(tfA.keySet());
        vocabulary.addAll(tfB.keySet());

        int vocabSize = vocabulary.size();
        if (vocabSize == 0) {
            return new VectorPair(new double[0], new double[0]);
        }

        double[] vecA = new double[vocabSize];
        double[] vecB = new double[vocabSize];

        int totalDocs = 2;
        int index = 0;
        for (String term : vocabulary) {
            int docFreq = (tfA.containsKey(term) ? 1 : 0) + (tfB.containsKey(term) ? 1 : 0);
            // Smoothed IDF
            double idf = Math.log(1.0 + ((double) totalDocs / (1.0 + docFreq))) + 1.0;

            double termTfA = tokensA.isEmpty() ? 0 : (double) tfA.getOrDefault(term, 0) / tokensA.size();
            double termTfB = tokensB.isEmpty() ? 0 : (double) tfB.getOrDefault(term, 0) / tokensB.size();

            vecA[index] = termTfA * idf;
            vecB[index] = termTfB * idf;
            index++;
        }

        return new VectorPair(vecA, vecB);
    }
}
