/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */

import java.sql.Date;

/**
 * Data Transfer Object cho Danh sách học viên trong lớp
 */
public class ClassStudentDTO {
    private int studentId;
    private String studentName;
    private String phone;
    private String parentName;
    private String className;
    private Date enrollDate;
    private String status;

    public ClassStudentDTO() {
    }

    public ClassStudentDTO(int studentId, String studentName, String phone, String parentName, String className, Date enrollDate, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.phone = phone;
        this.parentName = parentName;
        this.className = className;
        this.enrollDate = enrollDate;
        this.status = status;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Date getEnrollDate() { return enrollDate; }
    public void setEnrollDate(Date enrollDate) { this.enrollDate = enrollDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ClassStudentDTO{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", phone='" + phone + '\'' +
                ", parentName='" + parentName + '\'' +
                ", className='" + className + '\'' +
                ", enrollDate=" + enrollDate +
                ", status='" + status + '\'' +
                '}';
    }
}