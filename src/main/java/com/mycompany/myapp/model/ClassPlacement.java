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
 * Model quản lý việc xếp lớp cho học viên
 */
public class ClassPlacement {
    private int studentId;
    private int classId;
    private Date enrollDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public ClassPlacement() {
    }

    public ClassPlacement(int studentId, int classId, Date enrollDate, String status, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.studentId = studentId;
        this.classId = classId;
        this.enrollDate = enrollDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Date getEnrollDate() { return enrollDate; }
    public void setEnrollDate(Date enrollDate) { this.enrollDate = enrollDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "ClassPlacement{" +
                "studentId=" + studentId +
                ", classId=" + classId +
                ", enrollDate=" + enrollDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}