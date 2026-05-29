package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import com.mycompany.myapp.repository.AttendanceAnalyticsRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsService {

    private final AttendanceAnalyticsRepository repo = new AttendanceAnalyticsRepository();

    /*
     * FIX CHÍNH:
     * - Giáo viên: chỉ xem lớp được phân công.
     * - Giáo vụ / kế toán / admin: xem được tất cả lớp.
     *
     * Giữ tên hàm getTeacherClasses() để không phải sửa nhiều UI cũ.
     */
    public Result<List<Map<String, Object>>> getTeacherClasses() {
        int userId = SessionStore.getUserId();

        if (userId <= 0) {
            return Result.failure("Phiên đăng nhập hết hạn.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (isTeacherOnly()) {
                return Result.success(repo.getClassesByTeacher(conn, userId), "OK");
            }

            return Result.success(repo.getAllClasses(conn), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tải danh sách lớp.");
        }
    }

    /*
     * Hàm tên rõ nghĩa hơn cho các màn hình mới.
     */
    public Result<List<Map<String, Object>>> getAccessibleClasses() {
        return getTeacherClasses();
    }

    /*
     * classId = 0 nghĩa là xem tất cả lớp.
     * classId > 0 nghĩa là xem một lớp cụ thể.
     */
    public Result<List<AttendanceAnalyticsDTO>> getClassAnalytics(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            List<AttendanceAnalyticsDTO> data = repo.getAttendanceAnalytics(conn, classId);
            return Result.success(data, "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tính toán dữ liệu chuyên cần.");
        }
    }

    private boolean isTeacherOnly() {
        List<String> roles = SessionStore.getUserRoles();

        if (roles == null || roles.isEmpty()) {
            return false;
        }

        boolean isTeacher = false;
        boolean isStaffOrAdmin = false;

        for (String role : roles) {
            String r = normalize(role);

            if (r.equals("GIAO_VIEN") || r.equals("TEACHER")) {
                isTeacher = true;
            }

            if (r.equals("ADMIN")
                    || r.equals("QUAN_TRI_HE_THONG")
                    || r.equals("NHAN_VIEN_QUAN_LY_HE_THONG")
                    || r.equals("NHAN_VIEN_QUAN_LY_NGHIEP_VU")
                    || r.equals("ACADEMIC_STAFF")
                    || r.equals("GIAO_VU")
                    || r.equals("NHAN_VIEN_GIAO_VU")
                    || r.equals("NHAN_VIEN_KE_TOAN")
                    || r.equals("ACCOUNTANT")
                    || r.equals("KE_TOAN")) {
                isStaffOrAdmin = true;
            }
        }

        return isTeacher && !isStaffOrAdmin;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("Đ", "D");
    }
}
