package com.mycompany.myapp.model;

import java.math.BigDecimal;

/**
 * DTO phục vụ Use Case: Xuất bảng tổng hợp kết quả học tập cho Giáo vụ
 */
public class BangTongHopKetQuaDTO {
    
    private Long studentId;
    private String studentName; // Lấy từ bảng STUDENT
    private String className;   // Lấy từ bảng STUDY_CLASS
    private BigDecimal finalScore;
    private String rank;

    public BangTongHopKetQuaDTO() {
    }

    public BangTongHopKetQuaDTO(Long studentId, String studentName, String className, 
                                BigDecimal finalScore, String rank) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.finalScore = finalScore;
        this.rank = rank;
    }

    // Getters & Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    /**
     * Chuyển thành 1 dòng (Row) để hiển thị lên JTable cho Giáo vụ xem trước khi xuất file
     */
    public Object[] toTableRow(int stt) {
        // Xử lý hiển thị an toàn nếu chưa có điểm
        String scoreDisplay = (finalScore != null) ? finalScore.toString() : "Chưa có";
        String rankDisplay = (rank != null && !rank.isEmpty()) ? rank : "Chưa xét";

        return new Object[]{
            stt, 
            studentId, 
            studentName, 
            className, 
            scoreDisplay, 
            rankDisplay
        };
    }
}