package com.resumeanalyzer.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.model.ResumeAnalysis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalysisDao {

    private final ObjectMapper mapper = new ObjectMapper();

    public ResumeAnalysis save(ResumeAnalysis analysis) throws SQLException {
        String sql = """
            INSERT INTO resume_analyses (resume_id, overall_score, completeness_score, skill_coverage_score, education_score, experience_score, project_score, formatting_score, keyword_relevance_score, strengths, weaknesses, missing_info, improvement_suggestions)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, analysis.getResumeId());
            ps.setInt(2, analysis.getOverallScore());
            ps.setInt(3, analysis.getCompletenessScore());
            ps.setInt(4, analysis.getSkillCoverageScore());
            ps.setInt(5, analysis.getEducationScore());
            ps.setInt(6, analysis.getExperienceScore());
            ps.setInt(7, analysis.getProjectScore());
            ps.setInt(8, analysis.getFormattingScore());
            ps.setInt(9, analysis.getKeywordRelevanceScore());
            ps.setString(10, toJson(analysis.getStrengths()));
            ps.setString(11, toJson(analysis.getWeaknesses()));
            ps.setString(12, toJson(analysis.getMissingInfo()));
            ps.setString(13, toJson(analysis.getImprovementSuggestions()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    analysis.setId(rs.getLong(1));
                }
            }
        }
        return analysis;
    }

    public ResumeAnalysis findLatestByResumeId(Long resumeId) throws SQLException {
        String sql = "SELECT * FROM resume_analyses WHERE resume_id = ? ORDER BY analyzed_at DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, resumeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAnalysis(rs);
                }
            }
        }
        return null;
    }

    public int countTotalAnalyses() throws SQLException {
        String sql = "SELECT COUNT(*) FROM resume_analyses";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private ResumeAnalysis mapAnalysis(ResultSet rs) throws SQLException {
        ResumeAnalysis a = new ResumeAnalysis();
        a.setId(rs.getLong("id"));
        a.setResumeId(rs.getLong("resume_id"));
        a.setOverallScore(rs.getInt("overall_score"));
        a.setCompletenessScore(rs.getInt("completeness_score"));
        a.setSkillCoverageScore(rs.getInt("skill_coverage_score"));
        a.setEducationScore(rs.getInt("education_score"));
        a.setExperienceScore(rs.getInt("experience_score"));
        a.setProjectScore(rs.getInt("project_score"));
        a.setFormattingScore(rs.getInt("formatting_score"));
        a.setKeywordRelevanceScore(rs.getInt("keyword_relevance_score"));

        a.setStrengths(fromJson(rs.getString("strengths")));
        a.setWeaknesses(fromJson(rs.getString("weaknesses")));
        a.setMissingInfo(fromJson(rs.getString("missing_info")));
        a.setImprovementSuggestions(fromJson(rs.getString("improvement_suggestions")));

        Timestamp ts = rs.getTimestamp("analyzed_at");
        if (ts != null) a.setAnalyzedAt(ts.toLocalDateTime());
        return a;
    }

    private String toJson(List<String> list) {
        try {
            return mapper.writeValueAsString(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
