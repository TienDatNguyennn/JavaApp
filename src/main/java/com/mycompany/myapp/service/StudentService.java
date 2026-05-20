package com.mycompany.myapp.service;

import com.mycompany.myapp.repository.StudentDAO;
import com.mycompany.myapp.model.Student;
import java.sql.SQLException;
import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();

    public void addStudent(Student student) throws Exception {
        if (student.getFullName() == null || student.getFullName().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }
        if (student.getPhone() == null || !student.getPhone().matches("^0\\d{9}$")) {
            throw new Exception("Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 và có 10 số).");
        }
        
        try {
            studentDAO.insert(student);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { 
                throw new Exception("Số điện thoại này đã tồn tại trong hệ thống!");
            }
            throw new Exception("Lỗi hệ thống khi lưu học viên: " + e.getMessage());
        }
    }

    public List<Student> getAllStudents() throws Exception {
        try {
            return studentDAO.findAllActive();
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách học viên từ cơ sở dữ liệu.");
        }
    }
    
    public void deleteStudent(int studentId) throws Exception {
        try {
            studentDAO.softDelete(studentId);
        } catch (SQLException e) {
            throw new Exception("Lỗi khi xóa học viên.");
        }
    }
}