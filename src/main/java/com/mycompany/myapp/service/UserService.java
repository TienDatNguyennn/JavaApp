package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.repository.AccountRepository;
import com.mycompany.myapp.utils.PasswordUtil;
import com.mycompany.myapp.exception.DuplicateDataException; // Import Exception tự tạo

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {
    private final AccountRepository accountRepo = new AccountRepository();

    public void createUser(String fullName, String email, String phone, String username, String password, String confirm, int roleGroupId, String status) throws Exception {
        
        // ==========================================
        // BƯỚC 1: KIỂM TRA ĐỊNH DẠNG (VALIDATION)
        // ==========================================
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu tối thiểu 6 ký tự!");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }
        if (phone != null && !phone.trim().isEmpty() && !phone.startsWith("0")) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng số 0!");
        }

        // ==========================================
        // BƯỚC 2: KIỂM TRA TRÙNG LẶP (BUSINESS RULES)
        // ==========================================
        if (accountRepo.checkEmailExists(email)) {
            throw new DuplicateDataException("email", "Email này đã được đăng ký trong hệ thống!");
        }
        if (accountRepo.checkPhoneExists(phone)) {
            throw new DuplicateDataException("phone", "Số điện thoại này đã tồn tại!");
        }
        if (accountRepo.checkUsernameExists(username)) {
            throw new DuplicateDataException("username", "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!");
        }

        // ==========================================
        // BƯỚC 3: XỬ LÝ DATABASE (TRANSACTION)
        // ==========================================
        String hash = PasswordUtil.hashPassword(password);
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            // Insert theo đúng thứ tự khóa ngoại
            int userId = accountRepo.insertUser(conn, fullName, email, phone);
            int accountId = accountRepo.insertAccount(conn, userId, username, hash, status);
            accountRepo.insertAccountRoleGroup(conn, accountId, roleGroupId);

            conn.commit(); 
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); 
            throw new Exception("Lỗi hệ thống Database: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); 
                conn.close();
            }
        }
    }
}