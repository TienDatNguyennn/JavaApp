package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.TeacherScheduleDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository truy vấn thời khóa biểu giáo viên.
 *
 * Dữ liệu lấy từ:
 * - TEACHING_ASSIGNMENT: giáo viên được phân công lớp nào
 * - STUDY_CLASS: thông tin lớp, ngày bắt đầu, ngày kết thúc
 * - CLASS_SCHEDULE: thứ, giờ bắt đầu, giờ kết thúc
 * - SUBJECT: tên môn học
 * - ROOM: phòng học
 */
public class TeacherScheduleRepository {

    public List<TeacherScheduleDTO> getStandardSchedule(Long teacherId) {
        List<TeacherScheduleDTO> scheduleList = new ArrayList<>();

        if (teacherId == null || teacherId <= 0) {
            return scheduleList;
        }

        String sql = """
                SELECT cs.schedule_id,
                       cs.day_of_week,
                       cs.start_time,
                       cs.end_time,
                       sc.class_id,
                       sc.class_name,
                       sc.start_date,
                       sc.end_date,
                       sub.subject_name,
                       r.room_name
                FROM TEACHING_ASSIGNMENT ta
                INNER JOIN STUDY_CLASS sc
                        ON ta.class_id = sc.class_id
                INNER JOIN CLASS_SCHEDULE cs
                        ON sc.class_id = cs.class_id
                INNER JOIN SUBJECT sub
                        ON sc.subject_id = sub.subject_id
                INNER JOIN ROOM r
                        ON r.room_id = cs.room_id
                WHERE ta.teacher_id = ?
                  AND NVL(ta.is_deleted, 0) = 0
                  AND NVL(sc.is_deleted, 0) = 0
                  AND NVL(cs.is_deleted, 0) = 0
                  AND NVL(sub.is_deleted, 0) = 0
                  AND NVL(r.is_deleted, 0) = 0
                ORDER BY cs.day_of_week, cs.start_time
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, teacherId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TeacherScheduleDTO dto = new TeacherScheduleDTO();

                    dto.setScheduleId(rs.getLong("schedule_id"));
                    dto.setDayOfWeek(rs.getInt("day_of_week"));
                    dto.setStartTime(rs.getString("start_time"));
                    dto.setEndTime(rs.getString("end_time"));

                    dto.setClassId(rs.getLong("class_id"));
                    dto.setClassName(rs.getString("class_name"));

                    dto.setSubjectName(rs.getString("subject_name"));
                    dto.setRoomName(rs.getString("room_name"));

                    // Hai dòng quan trọng để UI hiển thị:
                    // "Môn/lớp bắt đầu khi nào và kết thúc khi nào"
                    dto.setStartDate(rs.getDate("start_date"));
                    dto.setEndDate(rs.getDate("end_date"));

                    scheduleList.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("[TeacherScheduleRepository] Lỗi khi truy vấn lịch giảng dạy: " + e.getMessage());
            e.printStackTrace();
        }

        return scheduleList;
    }
}