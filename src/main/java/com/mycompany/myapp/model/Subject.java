package com.mycompany.myapp.model;

/**
 * Entity ánh xạ với bảng COURSE trong Database.
 * Chứa toàn bộ thông tin gốc của một Khóa học/Môn học.
 */
public class Subject {
    
    private String SubjectId;       // Mã khóa học (Khóa chính)
    private String SubjectName;     // Tên khóa học
    private String description;    // Mô tả chi tiết chương trình học
    private String status;         // Trạng thái: "ACTIVE" (Đang mở) hoặc "DELETED" (Đã xóa)

    // ==========================================
    // 1. CONSTRUCTORS
    // ==========================================
    public Subject() {
    }

    public Subject(String SubjectId, String SubjectName, String description, int totalSessions, double tuitionFee, String status) {
        this.SubjectId = SubjectId;
        this.SubjectName = SubjectName;
        this.description = description;
        this.status = status;
    }

    // ==========================================
    // 2. GETTERS & SETTERS
    // ==========================================
    public String getSubjectId() {
        return SubjectId;
    }

    public void setSubjectId(String SubjectId) {
        this.SubjectId = SubjectId;
    }

    public String getSubjectName() {
        return SubjectName;
    }

    public void setCourseName(String SubjectName) {
        this.SubjectName = SubjectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ==========================================
    // 3. TO STRING (Hỗ trợ Debug)
    // ==========================================
    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + SubjectId + '\'' +
                ", courseName='" + SubjectName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}