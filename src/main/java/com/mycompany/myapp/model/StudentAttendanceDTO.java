package com.mycompany.myapp.model;

public class StudentAttendanceDTO {
    private int studentId;
    private String fullName;
    private int attendanceId; // = 0 nếu học sinh chưa được điểm danh lần nào trong ca này
    private String status;    // 'PRESENT' hoặc 'ABSENT' theo Database
    private String note;

    public StudentAttendanceDTO() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}