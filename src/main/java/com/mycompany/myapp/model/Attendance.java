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
 * Model quản lý điểm danh học viên
 */
public class Attendance {
    private int attendanceId;
    private int scheduleId;
    private int studentId;
    private Date attendanceDate;
    private String status;
    private String note;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public Attendance() {
    }

    public Attendance(int attendanceId, int scheduleId, int studentId, Date attendanceDate, String status, String note, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.attendanceId = attendanceId;
        this.scheduleId = scheduleId;
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", scheduleId=" + scheduleId +
                ", studentId=" + studentId +
                ", attendanceDate=" + attendanceDate +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}