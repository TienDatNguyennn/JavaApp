package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.repository.InvoiceDetailRepository;
import java.util.List;
import java.util.Map;

public class InvoiceDetailService {

    private final InvoiceDetailRepository repo = new InvoiceDetailRepository();

    public List<Map<String, Object>> getStudentSummary(String keyword) throws Exception {
        try { return repo.findStudentSummary(keyword); }
        catch (Exception e) { throw new Exception("Không thể tải danh sách học viên: " + e.getMessage()); }
    }

    public List<Map<String, Object>> getInvoicesByStudent(int studentId) throws Exception {
        try { return repo.findInvoicesByStudent(studentId); }
        catch (Exception e) { throw new Exception("Không thể tải danh sách hóa đơn: " + e.getMessage()); }
    }

    public List<Map<String, Object>> getDetailsByInvoice(int invoiceId) throws Exception {
        try { return repo.findDetailsByInvoice(invoiceId); }
        catch (Exception e) { throw new Exception("Không thể tải chi tiết hóa đơn: " + e.getMessage()); }
    }

    public List<Map<String, Object>> getAvailableClasses(int invoiceId) throws Exception {
        try { return repo.findAvailableClasses(invoiceId); }
        catch (Exception e) { throw new Exception("Không thể tải danh sách lớp: " + e.getMessage()); }
    }

    /** Tạo hóa đơn trống cho học viên, trả về invoice_id mới. */
    public int createBlankInvoice(int studentId) throws Exception {
        if (studentId <= 0) throw new Exception("Chưa chọn học viên!");
        try {
            int newId = repo.createBlankInvoice(studentId);
            DBConnection.commitTransaction();
            return newId;
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi tạo hóa đơn mới: " + e.getMessage());
        }
    }

    public void addDetail(int invoiceId, int classId, double amount) throws Exception {
        if (amount <= 0) throw new Exception("Học phí phải lớn hơn 0!");
        try {
            repo.insert(invoiceId, classId, amount);
            DBConnection.commitTransaction();
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi thêm chi tiết: " + e.getMessage());
        }
    }

    public void updateDetail(int invoiceId, int classId, double amount) throws Exception {
        if (amount <= 0) throw new Exception("Học phí phải lớn hơn 0!");
        try {
            repo.update(invoiceId, classId, amount);
            DBConnection.commitTransaction();
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi cập nhật chi tiết: " + e.getMessage());
        }
    }

    public void deleteDetail(int invoiceId, int classId) throws Exception {
        try {
            repo.softDelete(invoiceId, classId);
            DBConnection.commitTransaction();
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            throw new Exception("Lỗi xóa chi tiết: " + e.getMessage());
        }
    }
}
