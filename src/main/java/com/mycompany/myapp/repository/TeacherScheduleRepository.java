/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.repository;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.TeacherScheduleDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherScheduleRepository {

    public List<TeacherScheduleDTO> getStandardSchedule(Long teacherId) {
        List<TeacherScheduleDTO> scheduleList = new ArrayList<>();

        String sql = """
                 SELECT cs.schedule_id, cs.day_of_week, cs.start_time, cs.end_time,
                        sc.class_id, sc.class_name, sc.start_date, sc.end_date,
                        sub.subject_name, r.room_name
                 FROM TEACHING_ASSIGNMENT ta
                 INNER JOIN STUDY_CLASS sc ON ta.class_id = sc.class_id
                 INNER JOIN CLASS_SCHEDULE cs ON sc.class_id = cs.class_id
                 INNER JOIN SUBJECT sub ON sc.subject_id = sub.subject_id
                 INNER JOIN ROOM r ON r.room_id = cs.room_id
                 WHERE ta.teacher_id = ?
                 AND ta.is_deleted = 0 AND sc.is_deleted = 0 AND cs.is_deleted = 0
                 ORDER BY cs.day_of_week, cs.start_time
                 """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                    dto.setStartDate(rs.getDate("start_date"));
                    dto.setEndDate(rs.getDate("end_date"));

                    scheduleList.add(dto);


                }
            }
        }catch(SQLException e){
            System.err.println("Lỗi khi truy vấn lich giảng dạy" + e.getMessage());
            e.printStackTrace();
        }
        return scheduleList;

    }
    
    

}
