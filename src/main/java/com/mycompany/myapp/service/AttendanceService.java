package com.mycompany.myapp.service;

import com.mycompany.myapp.model.StudentAttendanceDTO;
import com.mycompany.myapp.repository.AttendanceRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.util.List;
import java.util.Map;

public class AttendanceService {
    
    private final AttendanceRepository repo = new AttendanceRepository();

    /**
     * 1. Lấy danh sách lớp học của Giáo viên đang đăng nhập
     */
    public Result<List<Map<String, Object>>> getTeacherClasses() {
        // Lấy ID giáo viên từ bộ lưu phiên Session nội bộ của bạn
        int teacherId = SessionStore.getUserId();
        if (teacherId <= 0) {
            return Result.failure("Lỗi phiên đăng nhập: Không tìm thấy định danh giảng viên.");
        }
        
        try {
            // Không cần truyền đối tượng Connection nữa, repo tự quản lý ngầm thông qua Pool
            List<Map<String, Object>> classes = repo.getClassesByTeacher(teacherId);
            return Result.success(classes, "Tải danh sách lớp học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi tải lớp học: " + e.getMessage());
        }
    }

    /**
     * 2. Lấy danh sách ca học (Lịch trình) của một lớp cụ thể
     */
    public Result<List<Map<String, Object>>> getClassSchedules(int classId) {
        if (classId <= 0) {
            return Result.failure("Mã lớp học không hợp lệ.");
        }
        
        try {
            List<Map<String, Object>> schedules = repo.getSchedulesByClass(classId);
            return Result.success(schedules, "Tải danh sách ca học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi tải ca học: " + e.getMessage());
        }
    }

    /**
     * 3. Lấy danh sách học viên phục vụ hiển thị lên lưới table điểm danh
     */
    public Result<List<StudentAttendanceDTO>> getAttendanceList(int classId, int scheduleId, java.util.Date date) {
        if (classId <= 0 || scheduleId <= 0 || date == null) {
            return Result.failure("Thông tin truy vấn bộ lọc danh sách điểm danh không hợp lệ.");
        }
        
        try {
            // Gọi hàm xử lý an toàn lỗi ORA-00904 lọc theo ngày chuẩn hóa TRUNC(created_at)
            List<StudentAttendanceDTO> attendanceList = repo.getAttendanceList(classId, scheduleId, date);
            return Result.success(attendanceList, "Tải danh sách dữ liệu điểm danh thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi tải danh sách học viên.");
        }
    }

    /**
     * 4. Lưu hoặc cập nhật danh sách điểm danh thủ công / tự động siêu tốc
     * Toàn bộ vòng lặp for-loop cồng kềnh check insert/update cũ đã được loại bỏ hoàn toàn
     */
    public Result<Void> saveAttendanceList(int scheduleId, java.util.Date date, List<StudentAttendanceDTO> list) {
        if (scheduleId <= 0 || date == null || list == null || list.isEmpty()) {
            return Result.failure("Không có dữ liệu điểm danh hợp lệ để tiến hành lưu.");
        }
        
        try {
            // Đẩy trực tiếp danh sách xuống DB, Oracle sẽ tự lo Transaction mượt mà
            repo.saveAttendanceList(scheduleId, date, list);
            return Result.success(null, "Lưu thông tin điểm danh lớp học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi xử lý lưu điểm danh: " + e.getMessage());
        }
    }
}