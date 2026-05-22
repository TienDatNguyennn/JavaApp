package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    
    public List<Student> findAllActive() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM STUDENT WHERE is_deleted = 0 ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    public void insert(Student s) throws SQLException {
        // Dùng MAX+1 để tránh lệch sequence với dữ liệu mẫu đã insert explicit ID
        String sql = "INSERT INTO STUDENT " +
                     "(student_id, full_name, dob, gender, phone, parent_name, parent_phone, address, managed_by) " +
                     "VALUES ((SELECT NVL(MAX(student_id),0)+1 FROM STUDENT), ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, s.getFullName());
            ps.setDate  (2, new java.sql.Date(s.getDob().getTime()));
            ps.setString(3, s.getGender());
            ps.setString(4, s.getPhone());
            ps.setNString(5, s.getParentName());
            ps.setString(6, s.getParentPhone());
            ps.setNString(7, s.getAddress());
            if (s.getManagedBy() != null && s.getManagedBy() > 0)
                ps.setInt(8, s.getManagedBy());
            else
                ps.setNull(8, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE STUDENT SET full_name=?, dob=?, gender=?, phone=?, " +
                     "parent_name=?, parent_phone=?, updated_at=SYSDATE " +
                     "WHERE student_id=? AND is_deleted=0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, s.getFullName());
            ps.setDate  (2, new java.sql.Date(s.getDob().getTime()));
            ps.setString(3, s.getGender());
            ps.setString(4, s.getPhone());
            ps.setNString(5, s.getParentName());
            ps.setString(6, s.getParentPhone());
            ps.setInt   (7, s.getStudentId());
            ps.executeUpdate();
        }
    }

    public void softDelete(int studentId) throws SQLException {
        String sql = "UPDATE STUDENT SET is_deleted = 1, updated_at = SYSDATE WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
    }
    
    private Student mapResultSetToEntity(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setFullName(rs.getNString("full_name"));
        s.setDob(rs.getDate("dob"));
        s.setGender(rs.getString("gender"));
        s.setPhone(rs.getString("phone"));
        s.setParentName(rs.getNString("parent_name"));
        s.setParentPhone(rs.getString("parent_phone"));
        s.setAddress(rs.getNString("address"));
        
        int managedBy = rs.getInt("managed_by");
        s.setManagedBy(rs.wasNull() ? null : managedBy);
        
        return s;
    }
}