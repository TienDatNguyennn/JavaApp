package com.mycompany.myapp.model;

public class AcademicReportDTO {
    private int studentId;
    private String fullName;
    private Double finalScore;
    private String rank;
    private String status; // "ĐẠT", "KHÔNG ĐẠT", hoặc "CHƯA CÓ ĐIỂM"

    public AcademicReportDTO() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}