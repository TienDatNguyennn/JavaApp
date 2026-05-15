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
 * Model quản lý phân công giảng dạy
 */
public class TeachingAssignment {
    private int assignId;
    private int teacherId;
    private int classId;
    private Date assignedDate;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public TeachingAssignment() {
    }

    public TeachingAssignment(int assignId, int teacherId, int classId, Date assignedDate, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.assignId = assignId;
        this.teacherId = teacherId;
        this.classId = classId;
        this.assignedDate = assignedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getAssignId() { return assignId; }
    public void setAssignId(int assignId) { this.assignId = assignId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Date getAssignedDate() { return assignedDate; }
    public void setAssignedDate(Date assignedDate) { this.assignedDate = assignedDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "TeachingAssignment{" +
                "assignId=" + assignId +
                ", teacherId=" + teacherId +
                ", classId=" + classId +
                ", assignedDate=" + assignedDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}