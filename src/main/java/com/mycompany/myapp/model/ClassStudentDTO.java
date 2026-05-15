package com.mycompany.myapp.model;

import java.util.Date;

public class ClassStudentDTO {
    private int studentId;
    private String fullName;
    private Date dob;
    private String gender; // 'M' hoặc 'F'
    private String phone;
    private String parentName;
    private String parentPhone;
    private String status; // Trạng thái học: ACTIVE, RESERVED, DROPPED

    public ClassStudentDTO() {}

    // GETTERS & SETTERS
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}