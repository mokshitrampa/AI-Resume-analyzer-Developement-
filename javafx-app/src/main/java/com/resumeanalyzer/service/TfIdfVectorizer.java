package com.resumeanalyzer.service;

import java.util.*;
import java.util.regex.Pattern;

public class TfIdfVectorizer {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
            "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
            "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't",
            "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor",
            "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out",
            "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some",
            "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
            "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to",
            "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
            "your", "yours", "yourself", "yourselves"
    ));

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-zA-Z0-9+#]+");

    public List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        String[] raw = TOKEN_SPLIT.split(text.toLowerCase());
        List<String> tokens = new ArrayList<>();
        for (String t : raw) {
            String trimmed = t.trim();
            if (trimmed.length() > 1 && !STOPWORDS.contains(trimmed)) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    public Map<String, Double> computeTfIdf(String docText, String backgroundText) {
        List<String> docTokens = tokenize(docText);
        List<String> bgTokens = tokenize(backgroundText);

        Map<String, Integer> termFreq = new HashMap<>();
        for (String token : docTokens) {
            termFreq.put(token, termFreq.getOrDefault(token, 0) + 1);
        }

        int totalDocTokens = Math.max(docTokens.size(), 1);
        Set<String> allVocab = new HashSet<>(docTokens);
        allVocab.addAll(bgTokens);

        int totalDocs = 2; // Pairwise: candidate document and job description
        Map<String, Double> tfIdfVector = new HashMap<>();

        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / totalDocTokens;

            int docsWithTerm = 1;
            if (bgTokens.contains(term)) {
                docsWithTerm++;
            }

            double idf = Math.log(1.0 + ((double) totalDocs / docsWithTerm));
            tfIdfVector.put(term, tf * idf);
        }

        return tfIdfVector;
    }
}
