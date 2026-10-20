package com.resumeanalyzer.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.model.ExtractedResumeData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExtractedDataDao {

    private final ObjectMapper mapper = new ObjectMapper();

    public ExtractedResumeData save(ExtractedResumeData data) throws SQLException {
        String sql = """
            INSERT INTO extracted_resume_data (resume_id, candidate_name, email, phone, location, degree, institution, graduation_year, cgpa, raw_text, technical_skills, experience_data, education_data, projects_data, certifications_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, data.getResumeId());
            ps.setString(2, data.getCandidateName());
            ps.setString(3, data.getEmail());
            ps.setString(4, data.getPhone());
            ps.setString(5, data.getLocation());
            ps.setString(6, data.getDegree());
            ps.setString(7, data.getInstitution());
            if (data.getGraduationYear() != null) ps.setInt(8, data.getGraduationYear()); else ps.setNull(8, Types.INTEGER);
            ps.setString(9, data.getCgpa());
            ps.setString(10, data.getRawText());
            ps.setString(11, toJson(data.getTechnicalSkills()));
            ps.setString(12, toJson(data.getExperience()));
            ps.setString(13, toJson(data.getEducation()));
            ps.setString(14, toJson(data.getProjects()));
            ps.setString(15, toJson(data.getCertifications()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    data.setId(rs.getLong(1));
                }
            }
        }
        return data;
    }

    public ExtractedResumeData findByResumeId(Long resumeId) throws SQLException {
        String sql = "SELECT * FROM extracted_resume_data WHERE resume_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, resumeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapExtracted(rs);
                }
            }
        }
        return null;
    }

    public void update(ExtractedResumeData data) throws SQLException {
        String sql = """
            UPDATE extracted_resume_data
            SET candidate_name = ?, email = ?, phone = ?, location = ?, degree = ?, institution = ?, graduation_year = ?, cgpa = ?, technical_skills = ?
            WHERE resume_id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.getCandidateName());
            ps.setString(2, data.getEmail());
            ps.setString(3, data.getPhone());
            ps.setString(4, data.getLocation());
            ps.setString(5, data.getDegree());
            ps.setString(6, data.getInstitution());
            if (data.getGraduationYear() != null) ps.setInt(7, data.getGraduationYear()); else ps.setNull(7, Types.INTEGER);
            ps.setString(8, data.getCgpa());
            ps.setString(9, toJson(data.getTechnicalSkills()));
            ps.setLong(10, data.getResumeId());
            ps.executeUpdate();
        }
    }

    private ExtractedResumeData mapExtracted(ResultSet rs) throws SQLException {
        ExtractedResumeData d = new ExtractedResumeData();
        d.setId(rs.getLong("id"));
        d.setResumeId(rs.getLong("resume_id"));
        d.setCandidateName(rs.getString("candidate_name"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setLocation(rs.getString("location"));
        d.setDegree(rs.getString("degree"));
        d.setInstitution(rs.getString("institution"));
        int gradYear = rs.getInt("graduation_year");
        if (!rs.wasNull()) d.setGraduationYear(gradYear);
        d.setCgpa(rs.getString("cgpa"));
        d.setRawText(rs.getString("raw_text"));
        d.setTechnicalSkills(fromJson(rs.getString("technical_skills")));
        d.setExperience(fromJson(rs.getString("experience_data")));
        d.setEducation(fromJson(rs.getString("education_data")));
        d.setProjects(fromJson(rs.getString("projects_data")));
        d.setCertifications(fromJson(rs.getString("certifications_data")));
        Timestamp ts = rs.getTimestamp("extracted_at");
        if (ts != null) d.setExtractedAt(ts.toLocalDateTime());
        return d;
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
