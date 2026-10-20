package com.resumeanalyzer.dao;

import com.resumeanalyzer.model.Job;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobDao {

    public Job create(Job job) throws SQLException {
        String sql = """
            INSERT INTO jobs (recruiter_id, title, company_name, location, work_mode, employment_type, salary_range, experience_level, min_experience_years, description, required_skills, preferred_skills, active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (job.getRecruiterId() != null) ps.setLong(1, job.getRecruiterId()); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getCompanyName());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getWorkMode());
            ps.setString(6, job.getEmploymentType());
            ps.setString(7, job.getSalaryRange());
            ps.setString(8, job.getExperienceLevel());
            ps.setInt(9, job.getMinExperienceYears());
            ps.setString(10, job.getDescription());
            ps.setString(11, String.join(", ", job.getRequiredSkills()));
            ps.setString(12, String.join(", ", job.getPreferredSkills()));
            ps.setBoolean(13, true);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    job.setId(rs.getLong(1));
                }
            }
        }
        return job;
    }

    public List<Job> findAllActive() throws SQLException {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE active = TRUE ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapJob(rs));
            }
        }
        return list;
    }

    public List<Job> findByRecruiterId(Long recruiterId) throws SQLException {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE recruiter_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recruiterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJob(rs));
                }
            }
        }
        return list;
    }

    public Job findById(Long id) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapJob(rs);
                }
            }
        }
        return null;
    }

    public void update(Job job) throws SQLException {
        String sql = """
            UPDATE jobs
            SET title = ?, company_name = ?, location = ?, work_mode = ?, employment_type = ?, salary_range = ?, experience_level = ?, min_experience_years = ?, description = ?, required_skills = ?, preferred_skills = ?, active = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getCompanyName());
            ps.setString(3, job.getLocation());
            ps.setString(4, job.getWorkMode());
            ps.setString(5, job.getEmploymentType());
            ps.setString(6, job.getSalaryRange());
            ps.setString(7, job.getExperienceLevel());
            ps.setInt(8, job.getMinExperienceYears());
            ps.setString(9, job.getDescription());
            ps.setString(10, String.join(", ", job.getRequiredSkills()));
            ps.setString(11, String.join(", ", job.getPreferredSkills()));
            ps.setBoolean(12, job.isActive());
            ps.setLong(13, job.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM jobs WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM jobs";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM jobs WHERE active = TRUE";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Job mapJob(ResultSet rs) throws SQLException {
        Job j = new Job();
        j.setId(rs.getLong("id"));
        long recruiterId = rs.getLong("recruiter_id");
        if (!rs.wasNull()) j.setRecruiterId(recruiterId);
        j.setTitle(rs.getString("title"));
        j.setCompanyName(rs.getString("company_name"));
        j.setLocation(rs.getString("location"));
        j.setWorkMode(rs.getString("work_mode"));
        j.setEmploymentType(rs.getString("employment_type"));
        j.setSalaryRange(rs.getString("salary_range"));
        j.setExperienceLevel(rs.getString("experience_level"));
        j.setMinExperienceYears(rs.getInt("min_experience_years"));
        j.setDescription(rs.getString("description"));
        j.setActive(rs.getBoolean("active"));

        String req = rs.getString("required_skills");
        if (req != null) {
            j.setRequiredSkills(Arrays.stream(req.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }
        String pref = rs.getString("preferred_skills");
        if (pref != null) {
            j.setPreferredSkills(Arrays.stream(pref.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) j.setCreatedAt(ts.toLocalDateTime());
        return j;
    }
}
