/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

/**
 *
 * @author Tien Dat
 */

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.repository.AccountRepository;
import com.mycompany.myapp.utils.PasswordUtil;
import java.sql.Connection;

public class UserService {
    private final AccountRepository accountRepo = new AccountRepository();

    // Đã cập nhật tham số: Thêm email, phone và đổi roleId thành roleGroupId
    public void createUser(String fullName, String email, String phone, String username, String password, String confirm, int roleGroupId, String status) throws Exception {
        // 1. Validate các điều kiện cơ bản
        if (password.length() < 6) throw new Exception("Mật khẩu tối thiểu 6 ký tự!");
        if (!password.equals(confirm)) throw new Exception("Mật khẩu xác nhận không khớp!");
        
        // Validate theo Constraint DB: phone bắt đầu bằng '0'
        if (phone != null && !phone.trim().isEmpty() && !phone.startsWith("0")) {
            throw new Exception("Số điện thoại phải bắt đầu bằng số 0!");
        }

        // Kiểm tra username đã tồn tại trong DB chưa
        if (accountRepo.existsByUsername(username)) throw new Exception("Username đã tồn tại!");

        // 2. Mã hóa mật khẩu
        String hash = PasswordUtil.hashPassword(password);
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction để bảo toàn dữ liệu

            // 3. Thực hiện insert vào 3 bảng liên kết (Cập nhật theo qltt (4).sql)
            
            // Bước 1: Thêm USERS (cần truyền thêm email, phone)
            int userId = accountRepo.insertUser(conn, fullName, email, phone);
            
            // Bước 2: Thêm ACCOUNT và lấy account_id trả về
            int accountId = accountRepo.insertAccount(conn, userId, username, hash, status);
            
            // Bước 3: Gán quyền vào ACCOUNT_ASSIGN_ROLE_GROUP (nối accountId với roleGroupId)
            accountRepo.insertAccountRoleGroup(conn, accountId, roleGroupId);

            conn.commit(); // Lưu thay đổi nếu tất cả thành công
        } catch (Exception e) {
            if (conn != null) conn.rollback(); // Hủy bỏ nếu có bất kỳ lỗi nào xảy ra
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset trạng thái trước khi đóng
                conn.close();
            }
        }
    }
}