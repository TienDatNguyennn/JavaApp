package com.mycompany.myapp.model;

/**
 * Entity ánh xạ với bảng COURSE trong Database.
 * Chứa toàn bộ thông tin gốc của một Khóa học/Môn học.
 */
public class Subject {
    
    private String SubjectId;       // Mã khóa học (Khóa chính)
    private String SubjectName;     // Tên khóa học
    private String description;    // Mô tả chi tiết chương trình học
    private int totalSessions;     // Tổng số buổi học quy định
    private double tuitionFee;     // Học phí tiêu chuẩn
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
        this.totalSessions = totalSessions;
        this.tuitionFee = tuitionFee;
        this.status = status;
    }

    // ==========================================
    // 2. GETTERS & SETTERS
    // ==========================================
    public String getCourseId() {
        return SubjectId;
    }

    public void setCourseId(String courseId) {
        this.SubjectId = courseId;
    }

    public String getCourseName() {
        return SubjectName;
    }

    public void setCourseName(String courseName) {
        this.SubjectName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(double tuitionFee) {
        this.tuitionFee = tuitionFee;
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
                ", totalSessions=" + totalSessions +
                ", tuitionFee=" + tuitionFee +
                ", status='" + status + '\'' +
                '}';
    }
}