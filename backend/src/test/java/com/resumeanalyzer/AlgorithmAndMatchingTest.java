package com.resumeanalyzer;

import com.resumeanalyzer.algorithm.CosineSimilarity;
import com.resumeanalyzer.algorithm.Stopwords;
import com.resumeanalyzer.algorithm.TextTokenizer;
import com.resumeanalyzer.algorithm.WeightedScoreCalculator;
import com.resumeanalyzer.service.SkillExtractionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmAndMatchingTest {

    @Test
    @DisplayName("Test Tokenizer and Stopword Filter")
    void testTokenizer() {
        String text = "Experienced Software Engineer with Java and Spring Boot development.";
        List<String> tokens = TextTokenizer.tokenize(text);

        assertFalse(tokens.contains("with"));
        assertFalse(tokens.contains("and"));
        assertTrue(tokens.contains("experienced"));
        assertTrue(tokens.contains("software"));
        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("spring"));
    }

    @Test
    @DisplayName("Test Cosine Similarity between Identical and Distinct Texts")
    void testCosineSimilarity() {
        String docA = "Java Spring Boot MySQL REST API microservices architecture";
        String docB = "Java Spring Boot MySQL REST API microservices architecture";
        int identicalScore = CosineSimilarity.calculatePercentage(docA, docB);
        assertEquals(100, identicalScore);

        String docC = "Cooking recipes culinary food pastry dessert baking";
        int distinctScore = CosineSimilarity.calculatePercentage(docA, docC);
        assertTrue(distinctScore < 10, "Completely disjoint documents should have near-zero similarity");
    }

    @Test
    @DisplayName("Test Weighted Scoring with Weight Redistribution")
    void testWeightedScoring() {
        // Full requirements
        WeightedScoreCalculator.CalculationResult resFull = WeightedScoreCalculator.calculate(
                100, true, // 40%
                100, true, // 20%
                100,       // 20%
                100, true, // 10%
                100, true  // 10%
        );
        assertEquals(100, resFull.getOverallScore());
        assertTrue(resFull.getExplanation().contains("Final Calculated Score: 100"));

        // When Preferred skills and Experience are omitted (weight redistribution)
        WeightedScoreCalculator.CalculationResult resRedistributed = WeightedScoreCalculator.calculate(
                80, true,  // 40%
                0, false,  // 0% (redistributed)
                60,        // 20%
                0, false,  // 0% (redistributed)
                100, true  // 10%
        );
        assertTrue(resRedistributed.getOverallScore() > 0);
        assertTrue(resRedistributed.getExplanation().contains("weight redistributed"));
    }

    @Test
    @DisplayName("Test Skill Extraction and Alias Mapping")
    void testSkillExtraction() {
        SkillExtractionService service = new SkillExtractionService(null);
        service.initSkillDictionary();

        String sampleResumeText = "Proficient in JS, SpringBoot, and PostgreSQL. Familiar with k8s and AWS.";
        List<String> skills = service.extractSkills(sampleResumeText);

        assertTrue(skills.contains("JavaScript"), "Alias 'JS' must resolve to 'JavaScript'");
        assertTrue(skills.contains("Spring Boot"), "Alias 'SpringBoot' must resolve to 'Spring Boot'");
        assertTrue(skills.contains("PostgreSQL"), "Alias 'PostgreSQL' must be extracted");
        assertTrue(skills.contains("Kubernetes"), "Alias 'k8s' must resolve to 'Kubernetes'");
        assertTrue(skills.contains("AWS"), "AWS must be extracted");
    }
}
