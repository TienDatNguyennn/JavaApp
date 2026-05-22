package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.model.SubjectDTO;
import com.mycompany.myapp.repository.StudyClassDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service quản lý lớp học & phân công xếp lớp.
 * Chịu trách nhiệm validation nghiệp vụ và điều phối commit/rollback.
 */
public class StudyClassService {

    private final StudyClassDAO dao = new StudyClassDAO();

    // ════════════════════════════════════════════════════════
    // STUDY_CLASS
    // ════════════════════════════════════════════════════════

    public List<Map<String, Object>> getClassesWithDetails() throws Exception {
        try {
            return dao.findAllWithDetails();
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách lớp học: " + e.getMessage());
        }
    }

    public List<StudyClass> getAllActiveClasses() throws Exception {
        try {
            return dao.findAllActive();
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách lớp học.");
        }
    }

    public void addClass(StudyClass sc) throws Exception {
        validateClass(sc);
        try {
            dao.insert(sc);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            if (e.getErrorCode() == 1) { // ORA-00001: unique constraint violated
                throw new Exception("Tên lớp học \"" + sc.getClassName() + "\" đã tồn tại!");
            }
            throw new Exception("Lỗi khi thêm lớp học: " + e.getMessage());
        }
    }

    public void updateClass(StudyClass sc) throws Exception {
        if (sc.getClassId() <= 0) throw new Exception("Không xác định được lớp cần cập nhật.");
        validateClass(sc);
        try {
            boolean updated = dao.update(sc);
            if (!updated) throw new Exception("Không tìm thấy lớp học ID=" + sc.getClassId());
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi khi cập nhật lớp học: " + e.getMessage());
        }
    }

    public void deleteClass(int classId) throws Exception {
        try {
            boolean deleted = dao.softDelete(classId);
            if (!deleted) throw new Exception("Không tìm thấy lớp học ID=" + classId);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi khi xóa lớp học: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // CLASS_PLACEMENT
    // ════════════════════════════════════════════════════════

    public List<Map<String, Object>> getStudentsInClass(int classId) throws Exception {
        try {
            return dao.findStudentsInClass(classId);
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách học viên trong lớp: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getStudentsNotInClass(int classId) throws Exception {
        try {
            return dao.findStudentsNotInClass(classId);
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách học viên khả dụng: " + e.getMessage());
        }
    }

    public void enrollStudent(int studentId, int classId) throws Exception {
        try {
            boolean result = dao.enrollStudent(studentId, classId);
            if (!result) throw new Exception("Học viên này đã được xếp vào lớp rồi!");
            DBConnection.commitTransaction();
        } catch (Exception e) {
            // Nếu là lỗi nghiệp vụ (đã xếp rồi) thì không cần rollback
            if (!e.getMessage().contains("đã được xếp")) {
                DBConnection.rollbackTransaction();
            }
            throw e;
        }
    }

    public void removeStudentFromClass(int studentId, int classId) throws Exception {
        try {
            boolean result = dao.removeStudentFromClass(studentId, classId);
            if (!result) throw new Exception("Không tìm thấy bản ghi xếp lớp để xóa.");
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi khi xóa học viên khỏi lớp: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // SUBJECT (dropdown helper)
    // ════════════════════════════════════════════════════════

    public List<SubjectDTO> getAllActiveSubjects() throws Exception {
        try {
            return dao.getAllActiveSubjects();
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách môn học: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // CLASS_SCHEDULE
    // ════════════════════════════════════════════════════════

    public List<Map<String, Object>> getSchedulesByClass(int classId) throws Exception {
        try { return dao.findSchedulesByClass(classId); }
        catch (SQLException e) { throw new Exception("Không thể tải lịch học: " + e.getMessage()); }
    }

    public void addSchedule(int classId, int roomId, int dayOfWeek,
                            String startTime, String endTime) throws Exception {
        validateTime(startTime, endTime);
        try {
            dao.insertSchedule(classId, roomId, dayOfWeek, startTime, endTime);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            if (e.getErrorCode() == 20002) throw new Exception("Phòng học đã bị trùng lịch trong khung giờ này!");
            throw new Exception("Lỗi khi thêm lịch học: " + e.getMessage());
        }
    }

    public void updateSchedule(int scheduleId, int roomId, int dayOfWeek,
                               String startTime, String endTime) throws Exception {
        validateTime(startTime, endTime);
        try {
            dao.updateSchedule(scheduleId, roomId, dayOfWeek, startTime, endTime);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            if (e.getErrorCode() == 20002) throw new Exception("Phòng học đã bị trùng lịch trong khung giờ này!");
            throw new Exception("Lỗi khi cập nhật lịch học: " + e.getMessage());
        }
    }

    public void deleteSchedule(int scheduleId) throws Exception {
        try {
            dao.softDeleteSchedule(scheduleId);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi khi xóa lịch học: " + e.getMessage());
        }
    }

    private void validateTime(String start, String end) throws Exception {
        if (!start.matches("\\d{2}:\\d{2}"))
            throw new Exception("Giờ bắt đầu không đúng định dạng HH:mm (vd: 07:30)!");
        if (!end.matches("\\d{2}:\\d{2}"))
            throw new Exception("Giờ kết thúc không đúng định dạng HH:mm (vd: 09:00)!");
        if (start.compareTo(end) >= 0)
            throw new Exception("Giờ kết thúc phải sau giờ bắt đầu!");
    }

    // ── Validation nghiệp vụ ──
    private void validateClass(StudyClass sc) throws Exception {
        if (sc.getClassName() == null || sc.getClassName().trim().isEmpty())
            throw new Exception("Tên lớp học không được để trống!");

        if (sc.getSubjectId() <= 0)
            throw new Exception("Vui lòng chọn môn học!");

        if (sc.getClassType() == null || (!sc.getClassType().equals("REG") && !sc.getClassType().equals("ADV")))
            throw new Exception("Loại lớp không hợp lệ (REG hoặc ADV)!");

        if (sc.getTuitionFee() < 0)
            throw new Exception("Học phí không được là số âm!");

        if (sc.getTeacherAllowance() < 0)
            throw new Exception("Phụ cấp giáo viên không được là số âm!");

        if (sc.getStartDate() == null)
            throw new Exception("Vui lòng nhập ngày khai giảng!");

        if (sc.getEndDate() == null)
            throw new Exception("Vui lòng nhập ngày kết thúc!");

        if (!sc.getEndDate().after(sc.getStartDate()))
            throw new Exception("Ngày kết thúc phải sau ngày khai giảng!");
    }
}
