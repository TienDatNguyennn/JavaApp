package com.mycompany.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ chính xác với bảng COURSE_RESULT (Kết quả học tập cuối khóa)
 */
public class CourseResult {
    
    private Long resultId;           // NUMBER(10) -> Long
    private Long studentId;          // NUMBER(10) -> Long (FK)
    private Long classId;            // NUMBER(10) -> Long (FK)
    private BigDecimal finalScore;   // NUMBER(5,2) -> BigDecimal
    private String rank;             // NVARCHAR2(20) -> String (Giỏi, Khá, TB)
    private LocalDateTime createdAt; // DATE -> LocalDateTime
    private LocalDateTime updatedAt; // DATE -> LocalDateTime
    private Boolean isDeleted;       // NUMBER(1) -> Boolean

    // ==========================================
    // 1. CONSTRUCTORS
    // ==========================================
    public CourseResult() {
    }

    public CourseResult(Long resultId, Long studentId, Long classId, BigDecimal finalScore, 
                        String rank, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.classId = classId;
        this.finalScore = finalScore;
        this.rank = rank;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    // ==========================================
    // 2. GETTERS & SETTERS
    // ==========================================
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}