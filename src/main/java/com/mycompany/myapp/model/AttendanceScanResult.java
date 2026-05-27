package com.mycompany.myapp.model;

public class AttendanceScanResult {

    public enum Status {
        SUCCESS,
        DUPLICATE,
        INVALID_QR,
        EXPIRED_QR,
        WRONG_SCHEDULE,
        SCHEDULE_NOT_FOUND,
        STUDENT_NOT_IN_CLASS,
        DB_ERROR
    }

    private final Status status;
    private final int studentId;
    private final int scheduleId;
    private final String message;

    public AttendanceScanResult(Status status, int studentId, int scheduleId, String message) {
        this.status = status;
        this.studentId = studentId;
        this.scheduleId = scheduleId;
        this.message = message;
    }

    public static AttendanceScanResult success(int studentId, int scheduleId) {
        return new AttendanceScanResult(
                Status.SUCCESS,
                studentId,
                scheduleId,
                "Điểm danh thành công."
        );
    }

    public static AttendanceScanResult duplicate(int studentId, int scheduleId) {
        return new AttendanceScanResult(
                Status.DUPLICATE,
                studentId,
                scheduleId,
                "Học viên đã điểm danh trước đó."
        );
    }

    public static AttendanceScanResult fail(Status status, int studentId, int scheduleId, String message) {
        return new AttendanceScanResult(status, studentId, scheduleId, message);
    }

    public Status getStatus() {
        return status;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}