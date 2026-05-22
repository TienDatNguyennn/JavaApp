package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.model.SubjectDTO;
import java.sql.*;
import java.util.*;

/**
 * DAO quản lý lớp học (STUDY_CLASS) và phân công học viên (CLASS_PLACEMENT).
 * Tất cả write operations cần caller gọi DBConnection.commitTransaction().
 */
public class StudyClassDAO {

    // ════════════════════════════════════════════════════════
    // STUDY_CLASS – CRUD
    // ════════════════════════════════════════════════════════

    /**
     * Lấy tất cả lớp kèm tên môn học và đếm số học viên (dùng cho bảng UI).
     */
    public List<Map<String, Object>> findAllWithDetails() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT sc.class_id, sc.class_name, " +
            "       NVL(sub.subject_name, '—') AS subject_name, " +
            "       sc.class_type, sc.tuition_fee, sc.teacher_allowance, " +
            "       sc.start_date, sc.end_date, " +
            "       (SELECT COUNT(*) FROM CLASS_PLACEMENT cp " +
            "        WHERE cp.class_id = sc.class_id AND cp.is_deleted = 0) AS student_count " +
            "FROM STUDY_CLASS sc " +
            "LEFT JOIN SUBJECT sub ON sc.subject_id = sub.subject_id " +
            "WHERE sc.is_deleted = 0 " +
            "ORDER BY sc.class_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("class_id",          rs.getInt("class_id"));
                row.put("class_name",         rs.getString("class_name"));
                row.put("subject_name",       rs.getString("subject_name"));
                row.put("class_type",         rs.getString("class_type"));
                row.put("tuition_fee",        rs.getDouble("tuition_fee"));
                row.put("teacher_allowance",  rs.getDouble("teacher_allowance"));
                row.put("start_date",         rs.getDate("start_date"));
                row.put("end_date",           rs.getDate("end_date"));
                row.put("student_count",      rs.getInt("student_count"));
                list.add(row);
            }
        }
        return list;
    }

    /** Lấy tất cả lớp active (dùng cho combobox). */
    public List<StudyClass> findAllActive() throws SQLException {
        List<StudyClass> list = new ArrayList<>();
        String sql = "SELECT class_id, subject_id, class_name, class_type, " +
                     "tuition_fee, teacher_allowance, start_date, end_date " +
                     "FROM STUDY_CLASS WHERE is_deleted = 0 ORDER BY class_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Lấy chi tiết 1 lớp theo ID. */
    public StudyClass findById(int classId) throws SQLException {
        String sql = "SELECT class_id, subject_id, class_name, class_type, " +
                     "tuition_fee, teacher_allowance, start_date, end_date " +
                     "FROM STUDY_CLASS WHERE class_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Thêm mới lớp học. */
    public boolean insert(StudyClass sc) throws SQLException {
        String sql =
            "INSERT INTO STUDY_CLASS " +
            "  (class_id, subject_id, class_name, class_type, tuition_fee, teacher_allowance, start_date, end_date) " +
            "VALUES ((SELECT NVL(MAX(class_id), 0) + 1 FROM STUDY_CLASS), ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sc.getSubjectId());
            ps.setNString(2, sc.getClassName());
            ps.setString(3, sc.getClassType());
            ps.setDouble(4, sc.getTuitionFee());
            if (sc.getTeacherAllowance() > 0) {
                ps.setDouble(5, sc.getTeacherAllowance());
            } else {
                ps.setNull(5, Types.NUMERIC);
            }
            ps.setDate(6, sc.getStartDate());
            ps.setDate(7, sc.getEndDate());
            return ps.executeUpdate() > 0;
        }
    }

    /** Cập nhật thông tin lớp học. */
    public boolean update(StudyClass sc) throws SQLException {
        String sql =
            "UPDATE STUDY_CLASS " +
            "SET subject_id = ?, class_name = ?, class_type = ?, " +
            "    tuition_fee = ?, teacher_allowance = ?, " +
            "    start_date = ?, end_date = ?, updated_at = SYSDATE " +
            "WHERE class_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sc.getSubjectId());
            ps.setNString(2, sc.getClassName());
            ps.setString(3, sc.getClassType());
            ps.setDouble(4, sc.getTuitionFee());
            if (sc.getTeacherAllowance() > 0) {
                ps.setDouble(5, sc.getTeacherAllowance());
            } else {
                ps.setNull(5, Types.NUMERIC);
            }
            ps.setDate(6, sc.getStartDate());
            ps.setDate(7, sc.getEndDate());
            ps.setInt(8, sc.getClassId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Xóa mềm lớp học. */
    public boolean softDelete(int classId) throws SQLException {
        String sql = "UPDATE STUDY_CLASS SET is_deleted = 1, updated_at = SYSDATE WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            return ps.executeUpdate() > 0;
        }
    }

    // ════════════════════════════════════════════════════════
    // CLASS_PLACEMENT – Quản lý xếp lớp
    // ════════════════════════════════════════════════════════

    /** Danh sách học viên đang học lớp classId. */
    public List<Map<String, Object>> findStudentsInClass(int classId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT s.student_id, s.full_name, s.phone, s.gender, " +
            "       s.parent_name, cp.enroll_date, cp.status " +
            "FROM CLASS_PLACEMENT cp " +
            "JOIN STUDENT s ON cp.student_id = s.student_id " +
            "WHERE cp.class_id = ? AND cp.is_deleted = 0 AND s.is_deleted = 0 " +
            "ORDER BY s.full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("student_id",  rs.getInt("student_id"));
                    row.put("full_name",   rs.getString("full_name"));
                    row.put("phone",       rs.getString("phone"));
                    row.put("gender",      rs.getString("gender"));
                    row.put("parent_name", rs.getString("parent_name"));
                    row.put("enroll_date", rs.getDate("enroll_date"));
                    row.put("status",      rs.getString("status"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /** Danh sách học viên active chưa xếp vào lớp classId. */
    public List<Map<String, Object>> findStudentsNotInClass(int classId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT s.student_id, s.full_name, s.phone, s.gender, s.parent_name " +
            "FROM STUDENT s " +
            "WHERE s.is_deleted = 0 " +
            "  AND NOT EXISTS (" +
            "      SELECT 1 FROM CLASS_PLACEMENT cp " +
            "      WHERE cp.student_id = s.student_id " +
            "        AND cp.class_id = ? " +
            "        AND cp.is_deleted = 0" +
            "  ) " +
            "ORDER BY s.full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("student_id",  rs.getInt("student_id"));
                    row.put("full_name",   rs.getString("full_name"));
                    row.put("phone",       rs.getString("phone"));
                    row.put("gender",      rs.getString("gender"));
                    row.put("parent_name", rs.getString("parent_name"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Xếp học viên vào lớp.
     * Nếu đã có bản ghi cũ (is_deleted=1) thì khôi phục, ngược lại INSERT mới.
     * @return true = thành công, false = đã xếp rồi (không cần làm gì thêm)
     */
    public boolean enrollStudent(int studentId, int classId) throws SQLException {
        // Kiểm tra bản ghi cũ
        String checkSql = "SELECT is_deleted FROM CLASS_PLACEMENT " +
                          "WHERE student_id = ? AND class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement chk = conn.prepareStatement(checkSql)) {
            chk.setInt(1, studentId);
            chk.setInt(2, classId);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("is_deleted") == 0) {
                        return false; // Đã đang học, không làm gì
                    }
                    // Bản ghi đã xóa mềm → khôi phục
                    String restoreSql =
                        "UPDATE CLASS_PLACEMENT " +
                        "SET is_deleted = 0, status = 'ACTIVE', " +
                        "    enroll_date = SYSDATE, updated_at = SYSDATE " +
                        "WHERE student_id = ? AND class_id = ?";
                    try (PreparedStatement upd = conn.prepareStatement(restoreSql)) {
                        upd.setInt(1, studentId);
                        upd.setInt(2, classId);
                        return upd.executeUpdate() > 0;
                    }
                }
            }
        }
        // Chưa có bản ghi → INSERT
        String insertSql =
            "INSERT INTO CLASS_PLACEMENT (student_id, class_id, status) " +
            "VALUES (?, ?, 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Xóa mềm học viên khỏi lớp (CLASS_PLACEMENT). */
    public boolean removeStudentFromClass(int studentId, int classId) throws SQLException {
        String sql =
            "UPDATE CLASS_PLACEMENT " +
            "SET is_deleted = 1, updated_at = SYSDATE " +
            "WHERE student_id = ? AND class_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        }
    }

    // ════════════════════════════════════════════════════════
    // HELPER – Môn học (dùng cho form dropdown)
    // ════════════════════════════════════════════════════════

    public List<SubjectDTO> getAllActiveSubjects() throws SQLException {
        List<SubjectDTO> list = new ArrayList<>();
        String sql = "SELECT subject_id, subject_name FROM SUBJECT " +
                     "WHERE is_deleted = 0 ORDER BY subject_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SubjectDTO dto = new SubjectDTO();
                dto.setSubjectId(rs.getInt("subject_id"));
                dto.setSubjectName(rs.getString("subject_name"));
                list.add(dto);
            }
        }
        return list;
    }

    // ════════════════════════════════════════════════════════
    // CLASS_SCHEDULE – Lịch học của lớp
    // ════════════════════════════════════════════════════════

    public List<Map<String, Object>> findSchedulesByClass(int classId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT cs.schedule_id, cs.day_of_week, cs.start_time, cs.end_time, " +
            "       cs.room_id, r.room_name " +
            "FROM CLASS_SCHEDULE cs " +
            "JOIN ROOM r ON cs.room_id = r.room_id " +
            "WHERE cs.class_id = ? AND cs.is_deleted = 0 " +
            "ORDER BY cs.day_of_week, cs.start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("schedule_id", rs.getInt("schedule_id"));
                    row.put("day_of_week", rs.getInt("day_of_week"));
                    row.put("start_time",  rs.getString("start_time"));
                    row.put("end_time",    rs.getString("end_time"));
                    row.put("room_id",     rs.getInt("room_id"));
                    row.put("room_name",   rs.getNString("room_name"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public boolean insertSchedule(int classId, int roomId, int dayOfWeek,
                                   String startTime, String endTime) throws SQLException {
        String sql =
            "INSERT INTO CLASS_SCHEDULE (schedule_id, class_id, room_id, day_of_week, start_time, end_time) " +
            "VALUES ((SELECT NVL(MAX(schedule_id),0)+1 FROM CLASS_SCHEDULE), ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, roomId);
            ps.setInt(3, dayOfWeek);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSchedule(int scheduleId, int roomId, int dayOfWeek,
                                   String startTime, String endTime) throws SQLException {
        String sql =
            "UPDATE CLASS_SCHEDULE SET room_id=?, day_of_week=?, start_time=?, end_time=?, " +
            "updated_at=SYSDATE WHERE schedule_id=? AND is_deleted=0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, dayOfWeek);
            ps.setString(3, startTime);
            ps.setString(4, endTime);
            ps.setInt(5, scheduleId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean softDeleteSchedule(int scheduleId) throws SQLException {
        String sql = "UPDATE CLASS_SCHEDULE SET is_deleted=1, updated_at=SYSDATE WHERE schedule_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Map ResultSet → StudyClass entity ──
    private StudyClass mapRow(ResultSet rs) throws SQLException {
        StudyClass sc = new StudyClass();
        sc.setClassId(rs.getInt("class_id"));
        sc.setSubjectId(rs.getInt("subject_id"));
        sc.setClassName(rs.getString("class_name"));
        sc.setClassType(rs.getString("class_type"));
        sc.setTuitionFee(rs.getDouble("tuition_fee"));
        sc.setTeacherAllowance(rs.getDouble("teacher_allowance"));
        sc.setStartDate(rs.getDate("start_date"));
        sc.setEndDate(rs.getDate("end_date"));
        return sc;
    }
}
