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
 * Data Transfer Object cho Tình trạng điểm danh của học viên
 */
public class StudentAttendanceDTO {
    private int studentId;
    private String studentName;
    private String className;
    private Date attendanceDate;
    private String attendanceStatus;
    private String note;

    public StudentAttendanceDTO() {
    }

    public StudentAttendanceDTO(int studentId, String studentName, String className, Date attendanceDate, String attendanceStatus, String note) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.attendanceDate = attendanceDate;
        this.attendanceStatus = attendanceStatus;
        this.note = note;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "StudentAttendanceDTO{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", attendanceDate=" + attendanceDate +
                ", attendanceStatus='" + attendanceStatus + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}