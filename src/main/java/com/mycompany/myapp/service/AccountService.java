/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.repository.AccountRepository;
// BẮT BUỘC IMPORT PasswordUtil vào đây
import com.mycompany.myapp.utils.PasswordUtil; 

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private AccountRepository repo = new AccountRepository();

    // Các hàm lấy danh sách và cập nhật trạng thái
    public List<RoleGroup> getRoles() throws SQLException { return repo.findAllRoleGroups(); }
    public List<AccountListDTO> getAccountsByFilter(String filter) throws SQLException { return repo.findAccountsByFilter(filter); }
    public void deleteAccount(int accountId) throws SQLException { repo.softDeleteAccount(accountId); }
    public void restoreAccount(int accountId) throws SQLException { repo.restoreAccount(accountId); }

    // =========================================================================
    // TRANSACTION: CẤP PHÁT TÀI KHOẢN KÈM BĂM MẬT KHẨU BCRYPT CHUẨN THỰC TẾ
    // =========================================================================
    public void createAccountTransaction(String fullName, String email, String phone, String username, String password, int roleGroupId, String status) throws Exception {
        // Kiểm tra trùng lặp Username
        if (repo.existsByUsername(username)) {
            throw new Exception("Tên đăng nhập đã tồn tại trong hệ thống!");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            // 1. Lưu thông tin cơ bản vào bảng USERS
            int newUserId = repo.insertUser(conn, fullName, email, phone);
            
            // 2. BĂM MẬT KHẨU (Hashing) 
            // Hệ thống sẽ lấy mật khẩu admin nhập (ví dụ: "123456") và biến nó thành chuỗi "$2a$12$..."
            String hashedPassword = PasswordUtil.hashPassword(password); 
            
            // 3. Lưu thông tin đăng nhập vào bảng ACCOUNT (lưu chuỗi đã băm, tuyệt đối không lưu pass gốc)
            int newAccountId = repo.insertAccount(conn, newUserId, username, hashedPassword, status);
            
            // 4. Gán quyền vào bảng ACCOUNT_ASSIGN_ROLE_GROUP
            repo.insertAccountRoleGroup(conn, newAccountId, roleGroupId);

            // Xác nhận lưu toàn bộ dữ liệu
            conn.commit(); 
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Hủy bỏ nếu có lỗi ở bất kỳ bước nào
            throw new Exception("Lỗi Database: " + e.getMessage());
        } finally {
            if (conn != null) { 
                conn.setAutoCommit(true); 
                conn.close(); 
            }
        }
    }
}