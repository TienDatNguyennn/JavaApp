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
 * Model quản lý lớp học
 */
public class StudyClass {
    private int classId;
    private int subjectId;
    private String className;
    private String classType;
    private double tuitionFee;
    private double teacherAllowance;
    private Date startDate;
    private Date endDate;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public StudyClass() {
    }

    public StudyClass(int classId, int subjectId, String className, String classType, double tuitionFee, double teacherAllowance, Date startDate, Date endDate, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.classId = classId;
        this.subjectId = subjectId;
        this.className = className;
        this.classType = classType;
        this.tuitionFee = tuitionFee;
        this.teacherAllowance = teacherAllowance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public double getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(double tuitionFee) { this.tuitionFee = tuitionFee; }

    public double getTeacherAllowance() { return teacherAllowance; }
    public void setTeacherAllowance(double teacherAllowance) { this.teacherAllowance = teacherAllowance; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "StudyClass{" +
                "classId=" + classId +
                ", subjectId=" + subjectId +
                ", className='" + className + '\'' +
                ", classType='" + classType + '\'' +
                ", tuitionFee=" + tuitionFee +
                ", teacherAllowance=" + teacherAllowance +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}