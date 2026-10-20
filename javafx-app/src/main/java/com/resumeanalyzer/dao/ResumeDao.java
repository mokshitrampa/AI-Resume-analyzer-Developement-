package com.resumeanalyzer.dao;

import com.resumeanalyzer.model.Resume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResumeDao {

    public Resume create(Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (user_id, original_filename, stored_filename, file_path, file_size, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, resume.getUserId());
            ps.setString(2, resume.getOriginalFilename());
            ps.setString(3, resume.getStoredFilename());
            ps.setString(4, resume.getFilePath());
            ps.setLong(5, resume.getFileSize());
            ps.setBoolean(6, true);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    resume.setId(rs.getLong(1));
                }
            }
        }
        return resume;
    }

    public List<Resume> findByUserId(Long userId) throws SQLException {
        List<Resume> list = new ArrayList<>();
        String sql = "SELECT * FROM resumes WHERE user_id = ? AND active = TRUE ORDER BY uploaded_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResume(rs));
                }
            }
        }
        return list;
    }

    public Resume findById(Long id) throws SQLException {
        String sql = "SELECT * FROM resumes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResume(rs);
                }
            }
        }
        return null;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM resumes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Resume mapResume(ResultSet rs) throws SQLException {
        Resume r = new Resume();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setOriginalFilename(rs.getString("original_filename"));
        r.setStoredFilename(rs.getString("stored_filename"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileSize(rs.getLong("file_size"));
        r.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) {
            r.setUploadedAt(ts.toLocalDateTime());
        }
        return r;
    }
}
