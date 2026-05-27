package com.mycompany.myapp.service;

import com.mycompany.myapp.repository.StudentDAO;
import com.mycompany.myapp.model.Student;

import java.sql.SQLException;
import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();

    /*
     * =========================================================
     * MODULE 5 - DEADLOCK DEMO SWITCH
     *
     * NORMAL:
     *     Sản phẩm chạy bình thường.
     *     updateStudent() chỉ gọi studentDAO.update(student).
     *
     * DEADLOCK_DEMO:
     *     Dùng khi demo trực tiếp trên sản phẩm.
     *     updateStudent() sẽ khóa:
     *     STUDENT -> INVOICE
     *
     * Khi demo lỗi:
     *     StudentService: DEADLOCK_DEMO
     *     InvoiceDetailRepository: ERROR_DEADLOCK
     *
     * Khi demo fix:
     *     StudentService: DEADLOCK_DEMO
     *     InvoiceDetailRepository: FIXED
     *
     * Khi nộp/chạy sản phẩm bình thường:
     *     StudentService: NORMAL
     *     InvoiceDetailRepository: FIXED
     * =========================================================
     */
    private enum StudentUpdateMode {
        NORMAL,
        DEADLOCK_DEMO
    }

    /*
     * ĐỔI Ở ĐÂY KHI DEMO
     *
     * Bình thường:
     * private static final StudentUpdateMode UPDATE_MODE = StudentUpdateMode.NORMAL;
     *
     * Demo deadlock/fix:
     * private static final StudentUpdateMode UPDATE_MODE = StudentUpdateMode.DEADLOCK_DEMO;
     */
    private static final StudentUpdateMode UPDATE_MODE = StudentUpdateMode.NORMAL;

    public void addStudent(Student student) throws Exception {
        validateStudentForSave(student, false);

        try {
            studentDAO.insert(student);

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

                if (msg.contains("phone")) {
                    throw new Exception("Số điện thoại này đã tồn tại trong hệ thống!");
                }

                throw new Exception("Dữ liệu bị trùng, vi phạm ràng buộc duy nhất. Chi tiết: " + e.getMessage());
            }

            throw new Exception("Lỗi hệ thống khi lưu học viên: " + e.getMessage());
        }
    }

    public void updateStudent(Student student) throws Exception {
        validateStudentForSave(student, true);

        try {
            if (UPDATE_MODE == StudentUpdateMode.DEADLOCK_DEMO) {
                /*
                 * =====================================================
                 * MODULE 5 - DEMO TRỰC TIẾP TRÊN CHỨC NĂNG SỬA HỌC VIÊN
                 *
                 * Bước 1:
                 * Tìm hóa đơn gần nhất của học viên.
                 *
                 * Bước 2:
                 * Gọi updateForDeadlockDemo().
                 *
                 * Trong StudentDAO, hàm này sẽ khóa:
                 * STUDENT -> INVOICE
                 *
                 * Nếu bên học phí đang khóa ngược:
                 * INVOICE -> STUDENT
                 *
                 * => sinh deadlock / lock wait timeout.
                 * =====================================================
                 */
                int invoiceId = studentDAO.findLatestInvoiceIdByStudent(student.getStudentId());

                System.out.println("========== MODULE 5 - STUDENT UPDATE DEMO ==========");
                System.out.println("[SERVICE] UPDATE_MODE = DEADLOCK_DEMO");
                System.out.println("[SERVICE] student_id = " + student.getStudentId());
                System.out.println("[SERVICE] invoice_id = " + invoiceId);
                System.out.println("[SERVICE] Gọi StudentDAO.updateForDeadlockDemo()");
                System.out.println("[SERVICE] Thứ tự khóa bên học viên: STUDENT -> INVOICE");

                studentDAO.updateForDeadlockDemo(student, invoiceId);

            } else {
                /*
                 * =====================================================
                 * BẢN BÌNH THƯỜNG
                 * Không khóa INVOICE, chỉ update STUDENT.
                 * =====================================================
                 */
                System.out.println("[SERVICE] UPDATE_MODE = NORMAL");
                studentDAO.update(student);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new Exception("Số điện thoại này đã tồn tại trong hệ thống!");
            }

            if (isLockError(e)) {
                throw new Exception(
                    "Demo Deadlock: giao dịch cập nhật học viên bị rollback do tranh chấp khóa. " +
                    "Chi tiết: " + e.getMessage()
                );
            }

            throw new Exception("Lỗi cập nhật học viên: " + e.getMessage());
        }
    }

    public List<Student> getAllStudents() throws Exception {
        try {
            return studentDAO.findAllActive();

        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách học viên từ cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public void deleteStudent(int studentId) throws Exception {
        if (studentId <= 0) {
            throw new Exception("Không xác định được học viên cần xóa.");
        }

        try {
            studentDAO.softDelete(studentId);

        } catch (SQLException e) {
            throw new Exception("Lỗi khi xóa học viên: " + e.getMessage());
        }
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */
    private void validateStudentForSave(Student student, boolean isUpdate) throws Exception {
        if (student == null) {
            throw new Exception("Dữ liệu học viên không hợp lệ.");
        }

        if (isUpdate && student.getStudentId() <= 0) {
            throw new Exception("Không xác định được học viên cần cập nhật.");
        }

        if (student.getFullName() == null || student.getFullName().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }

        if (student.getPhone() == null || !student.getPhone().matches("^0\\d{9}$")) {
            throw new Exception("Số điện thoại không hợp lệ, phải bắt đầu bằng 0 và có 10 số.");
        }

        if (student.getDob() == null) {
            throw new Exception("Ngày sinh không được để trống.");
        }
    }

    /*
     * =========================================================
     * ORACLE LOCK ERROR CHECK
     *
     * ORA-00060:
     *     Deadlock detected.
     *
     * ORA-30006:
     *     Resource busy; acquire with WAIT timeout expired.
     * =========================================================
     */
    private boolean isLockError(SQLException e) {
        int code = e.getErrorCode();
        String msg = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        return code == 60
            || code == 30006
            || msg.contains("ORA-00060")
            || msg.contains("ORA-30006")
            || msg.contains("DEADLOCK")
            || msg.contains("RESOURCE BUSY");
    }
}