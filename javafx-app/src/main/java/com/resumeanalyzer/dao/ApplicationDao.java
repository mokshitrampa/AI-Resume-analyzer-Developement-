package com.resumeanalyzer.dao;

import com.resumeanalyzer.model.JobApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDao {

    public JobApplication create(JobApplication app) throws SQLException {
        String sql = """
            INSERT INTO job_applications (job_id, student_id, resume_id, match_score, status, cover_letter, recruiter_notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, app.getJobId());
            ps.setLong(2, app.getStudentId());
            ps.setLong(3, app.getResumeId());
            ps.setInt(4, app.getMatchScore());
            ps.setString(5, app.getStatus());
            ps.setString(6, app.getCoverLetter());
            ps.setString(7, app.getRecruiterNotes());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    app.setId(rs.getLong(1));
                }
            }
        }
        return app;
    }

    public List<JobApplication> findByJobId(Long jobId) throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        String sql = """
            SELECT a.*, j.title AS job_title, j.company_name AS company_name,
                   u.full_name AS student_name, u.email AS student_email, u.phone AS student_phone,
                   r.original_filename AS resume_filename
            FROM job_applications a
            JOIN jobs j ON a.job_id = j.id
            JOIN users u ON a.student_id = u.id
            JOIN resumes r ON a.resume_id = r.id
            WHERE a.job_id = ?
            ORDER BY a.match_score DESC, a.applied_at DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapApp(rs));
                }
            }
        }
        return list;
    }

    public List<JobApplication> findByStudentId(Long studentId) throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        String sql = """
            SELECT a.*, j.title AS job_title, j.company_name AS company_name,
                   u.full_name AS student_name, u.email AS student_email, u.phone AS student_phone,
                   r.original_filename AS resume_filename
            FROM job_applications a
            JOIN jobs j ON a.job_id = j.id
            JOIN users u ON a.student_id = u.id
            JOIN resumes r ON a.resume_id = r.id
            WHERE a.student_id = ?
            ORDER BY a.applied_at DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapApp(rs));
                }
            }
        }
        return list;
    }

    public boolean hasApplied(Long jobId, Long studentId) throws SQLException {
        String sql = "SELECT 1 FROM job_applications WHERE job_id = ? AND student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateStatus(Long id, String status, String notes) throws SQLException {
        String sql = "UPDATE job_applications SET status = ?, recruiter_notes = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM job_applications";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countShortlisted() throws SQLException {
        String sql = "SELECT COUNT(*) FROM job_applications WHERE status = 'SHORTLISTED'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private JobApplication mapApp(ResultSet rs) throws SQLException {
        JobApplication a = new JobApplication();
        a.setId(rs.getLong("id"));
        a.setJobId(rs.getLong("job_id"));
        a.setStudentId(rs.getLong("student_id"));
        a.setResumeId(rs.getLong("resume_id"));
        a.setMatchScore(rs.getInt("match_score"));
        a.setStatus(rs.getString("status"));
        a.setCoverLetter(rs.getString("cover_letter"));
        a.setRecruiterNotes(rs.getString("recruiter_notes"));

        a.setJobTitle(rs.getString("job_title"));
        a.setCompanyName(rs.getString("company_name"));
        a.setStudentName(rs.getString("student_name"));
        a.setStudentEmail(rs.getString("student_email"));
        a.setStudentPhone(rs.getString("student_phone"));
        a.setResumeFilename(rs.getString("resume_filename"));

        Timestamp ts = rs.getTimestamp("applied_at");
        if (ts != null) a.setAppliedAt(ts.toLocalDateTime());
        return a;
    }
}
