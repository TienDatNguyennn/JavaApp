package com.mycompany.myapp.model;

public class CourseResultDTO {
    private int studentId;
    private String fullName;
    private int resultId; // 0 nếu học sinh chưa có điểm
    private Double finalScore; // Dùng Double (Class) để có thể gán null nếu chưa nhập điểm
    private String rank;

    public CourseResultDTO() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
}