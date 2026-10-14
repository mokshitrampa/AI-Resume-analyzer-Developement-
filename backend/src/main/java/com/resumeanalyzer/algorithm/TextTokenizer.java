package com.resumeanalyzer.algorithm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTokenizer {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z0-9+#.]+");

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<String> tokens = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            String token = matcher.group().trim();
            // Clean trailing period
            if (token.endsWith(".") && token.length() > 1 && !token.equalsIgnoreCase("c++") && !token.equalsIgnoreCase(".net")) {
                token = token.substring(0, token.length() - 1);
            }
            if (token.length() > 1 && !Stopwords.isStopword(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static Map<String, Integer> getTermFrequencies(List<String> tokens) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String t : tokens) {
            freqMap.put(t, freqMap.getOrDefault(t, 0) + 1);
        }
        return freqMap;
    }
}
