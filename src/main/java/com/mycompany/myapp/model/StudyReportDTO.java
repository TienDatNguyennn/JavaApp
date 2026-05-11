package com.mycompany.myapp.model;

public class StudyReportDTO {
    private String studentId;
    private String fullName;
    private String className;
    private double finalScore;
    private String classification;
    private String result;

    public StudyReportDTO() {}

    public StudyReportDTO(String studentId, String fullName, String className, double finalScore) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
        this.finalScore = finalScore;
        calculateResultAndClassification(); // Tự động tính toán khi có điểm
    }

    // Logic nghiệp vụ tự động phân loại
    private void calculateResultAndClassification() {
        if (finalScore >= 8.0) this.classification = "Giỏi";
        else if (finalScore >= 6.5) this.classification = "Khá";
        else if (finalScore >= 5.0) this.classification = "Trung bình";
        else this.classification = "Yếu";

        this.result = (finalScore >= 5.0) ? "ĐẠT" : "CHƯA ĐẠT";
    }

    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getClassName() { return className; }
    public double getAverageScore() { return finalScore; }
    public String getClassification() { return classification; }
    public String getResult() { return result; }
}