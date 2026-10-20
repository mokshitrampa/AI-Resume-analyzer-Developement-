package com.resumeanalyzer.dao;

import com.resumeanalyzer.model.Skill;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillDao {

    public List<Skill> findAll() throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM skills ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSkill(rs));
            }
        }
        return list;
    }

    public Skill create(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (name, category, aliases) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skill.getName());
            ps.setString(2, skill.getCategory());
            ps.setString(3, String.join(", ", skill.getAliases()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    skill.setId(rs.getLong(1));
                }
            }
        }
        return skill;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM skills WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM skills";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Skill mapSkill(ResultSet rs) throws SQLException {
        Skill s = new Skill();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setCategory(rs.getString("category"));
        String aliasesStr = rs.getString("aliases");
        if (aliasesStr != null && !aliasesStr.trim().isEmpty()) {
            s.setAliases(Arrays.stream(aliasesStr.split(","))
                    .map(String::trim).filter(a -> !a.isEmpty()).collect(Collectors.toList()));
        }
        return s;
    }
}
