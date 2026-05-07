/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

import java.text.DecimalFormat;

/**
 *
 * @author PC
 */
public class SubjectDTO {
    private String SubjectId;
    private String SubjectName;
    private int totalSessions;
    private double tuitionFee;
    private String status;
    
    // Trường bổ sung: Đếm số lớp học đang dạy khóa này (Nếu team có yêu cầu thống kê)
    // private int activeClassesCount; 

    public SubjectDTO() {
    }

    public SubjectDTO(String SubjectId, String SubjectName, int totalSessions, double tuitionFee, String status) {
        this.SubjectId = SubjectId;
        this.SubjectName = SubjectName;
        this.totalSessions = totalSessions;
        this.tuitionFee = tuitionFee;
        this.status = status;
    }

    // Getters & Setters
    public String getSubjectId() { return SubjectId; }
    public void setSubjectId(String courseId) { this.SubjectId = courseId; }
    public String getSubjectName() { return SubjectName; }
    public void setSubjectName(String courseName) { this.SubjectName = courseName; }
    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public double getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(double tuitionFee) { this.tuitionFee = tuitionFee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Hàm định dạng tiền tệ (VNĐ) để hiển thị đẹp mắt trên giao diện
     */
    private String getFormattedTuitionFee() {
        DecimalFormat formatter = new DecimalFormat("###,###,### VNĐ");
        return formatter.format(tuitionFee);
    }

    /**
     * Hàm xử lý hiển thị trạng thái bằng Tiếng Việt
     */
    private String getDisplayStatus() {
        if ("ACTIVE".equalsIgnoreCase(status)) return "Đang hoạt động";
        if ("DELETED".equalsIgnoreCase(status)) return "Đã tạm ngưng";
        return status;
    }

    /**
     * TÍNH NĂNG ĐẶC BIỆT: Chuyển thẳng Object thành 1 dòng (Row) cho JTable
     * @param stt Số thứ tự của dòng trên bảng
     * @return Mảng Object tương ứng với các cột trên bảng
     */
    public Object[] toTableRow(int stt) {
        return new Object[]{
            stt, 
            this.SubjectId, 
            this.SubjectName, 
            this.totalSessions + " buổi", 
            getFormattedTuitionFee(), 
            getDisplayStatus()
        };
    }
}
