package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AttendanceScanResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class ConcurrencyAttendanceRepository {

    public AttendanceScanResult processAttendanceWithLock(
            int scheduleId,
            int studentId,
            String qrToken
    ) {
        if (scheduleId <= 0) {
            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.SCHEDULE_NOT_FOUND,
                    studentId,
                    scheduleId,
                    "scheduleId không hợp lệ."
            );
        }

        if (studentId <= 0) {
            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.DB_ERROR,
                    studentId,
                    scheduleId,
                    "studentId không hợp lệ."
            );
        }

        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!isScheduleExists(conn, scheduleId)) {
                    conn.rollback();
                    return AttendanceScanResult.fail(
                            AttendanceScanResult.Status.SCHEDULE_NOT_FOUND,
                            studentId,
                            scheduleId,
                            "Không tìm thấy lịch học schedule_id = " + scheduleId
                    );
                }

                if (!isStudentExists(conn, studentId)) {
                    conn.rollback();
                    return AttendanceScanResult.fail(
                            AttendanceScanResult.Status.DB_ERROR,
                            studentId,
                            scheduleId,
                            "Không tìm thấy học viên student_id = " + studentId
                    );
                }

                /*
                 * Chuẩn nghiệp vụ:
                 * Chỉ cho điểm danh nếu học viên thuộc lớp của lịch học.
                 *
                 * Nếu database của bạn chưa có bảng CLASS_ENROLLMENT
                 * hoặc tên bảng khác, sửa lại query trong hàm isStudentInSchedule().
                 */
                if (!isStudentInSchedule(conn, scheduleId, studentId)) {
                    conn.rollback();
                    return AttendanceScanResult.fail(
                            AttendanceScanResult.Status.STUDENT_NOT_IN_CLASS,
                            studentId,
                            scheduleId,
                            "Học viên không thuộc lớp của lịch học này."
                    );
                }

                insertAttendance(conn, scheduleId, studentId, qrToken);

                conn.commit();
                return AttendanceScanResult.success(studentId, scheduleId);

            } catch (SQLIntegrityConstraintViolationException e) {
                conn.rollback();

                return AttendanceScanResult.duplicate(studentId, scheduleId);

            } catch (SQLException e) {
                conn.rollback();

                System.err.println("[ATTENDANCE DB ERROR]");
                System.err.println("studentId = " + studentId);
                System.err.println("scheduleId = " + scheduleId);
                System.err.println("errorCode = " + e.getErrorCode());
                System.err.println("sqlState = " + e.getSQLState());
                System.err.println("message = " + e.getMessage());
                e.printStackTrace();

                return AttendanceScanResult.fail(
                        AttendanceScanResult.Status.DB_ERROR,
                        studentId,
                        scheduleId,
                        "Lỗi DB: " + e.getMessage()
                );
            }

        } catch (SQLException e) {
            System.err.println("[CONNECTION ERROR] " + e.getMessage());
            e.printStackTrace();

            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.DB_ERROR,
                    studentId,
                    scheduleId,
                    "Không mở được connection: " + e.getMessage()
            );
        }
    }

    private boolean isScheduleExists(Connection conn, int scheduleId) throws SQLException {
        String sql =
                "SELECT 1 " +
                "FROM CLASS_SCHEDULE " +
                "WHERE schedule_id = ? " +
                "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isStudentExists(Connection conn, int studentId) throws SQLException {
        String sql =
                "SELECT 1 " +
                "FROM STUDENT " +
                "WHERE student_id = ? " +
                "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isStudentInSchedule(Connection conn, int scheduleId, int studentId) throws SQLException {
        String sql =
                "SELECT 1 " +
                "FROM CLASS_SCHEDULE cs " +
                "JOIN CLASS_ENROLLMENT ce ON ce.class_id = cs.class_id " +
                "WHERE cs.schedule_id = ? " +
                "  AND ce.student_id = ? " +
                "  AND cs.is_deleted = 0 " +
                "  AND ce.is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertAttendance(
            Connection conn,
            int scheduleId,
            int studentId,
            String qrToken
    ) throws SQLException {

        String sql =
                "INSERT INTO ATTENDANCE " +
                "    (schedule_id, student_id, status, qr_token, attendance_time, created_at) " +
                "VALUES " +
                "    (?, ?, 'PRESENT', ?, CURRENT_TIMESTAMP, SYSDATE)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, studentId);
            ps.setString(3, qrToken);
            ps.executeUpdate();
        }
    }
}