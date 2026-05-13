package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.exception.DuplicateDataException;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.AccountUpdateDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.repository.AccountRepository;
import com.mycompany.myapp.utils.AccountValidator;
import com.mycompany.myapp.utils.PasswordUtil;
import com.mycompany.myapp.utils.Result;

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
    // CÁC HÀM TRẢ VỀ BOOLEAN ĐỂ CONTROLLER KIỂM TRA TRƯỚC KHI THÊM
    // =========================================================================
    
    public boolean isEmailExists(String email) throws SQLException {
        return repo.checkEmailExists(email);
    }

    public boolean isPhoneExists(String phone) throws SQLException {
        return repo.checkPhoneExists(phone);
    }

    public boolean isUsernameExists(String username) throws SQLException {
        return repo.checkUsernameExists(username);
    }

    // =========================================================================
    // TRANSACTION: CẤP PHÁT TÀI KHOẢN KÈM BĂM MẬT KHẨU BCRYPT
    // =========================================================================
    public void createAccountTransaction(String fullName, String email, String phone, String username, String password, int roleGroupId, String status) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            // 1. Lưu thông tin cơ bản vào bảng USERS
            int newUserId = repo.insertUser(conn, fullName, email, phone);
            
            // 2. BĂM MẬT KHẨU (Hashing) 
            String hashedPassword = PasswordUtil.hashPassword(password); 
            
            // 3. Lưu thông tin đăng nhập vào bảng ACCOUNT
            int newAccountId = repo.insertAccount(conn, newUserId, username, hashedPassword, status);
            
            // 4. Gán quyền vào bảng ACCOUNT_ASSIGN_ROLE_GROUP
            repo.insertAccountRoleGroup(conn, newAccountId, roleGroupId);

            conn.commit(); 
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); 
            throw new Exception("Lỗi Database: " + e.getMessage());
        } finally {
            if (conn != null) { 
                conn.setAutoCommit(true); 
                conn.close(); 
            }
        }
    }

    // =========================================================================
    // MODULE: CẬP NHẬT TÀI KHOẢN (THÔNG TIN & MẬT KHẨU)
    // =========================================================================

    public Result<AccountUpdateDTO> getAccountInfo(int accountId) {
        try (Connection conn = DBConnection.getConnection()) {
            AccountUpdateDTO info = repo.getAccountFullInfo(conn, accountId);
            if (info == null) {
                return Result.failure("Tài khoản không tồn tại hoặc đã bị khóa.");
            }
            return Result.success(info, "Lấy thông tin thành công");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public Result<Void> updateProfile(AccountUpdateDTO dto) {
        Connection conn = null;
        try {
            AccountValidator.validateProfile(dto);
            
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            if (repo.isDuplicateInfo(conn, "email", dto.getEmail(), dto.getUserId())) {
                throw new DuplicateDataException("email", "Email đã được sử dụng bởi người dùng khác!");
            }
            if (repo.isDuplicateInfo(conn, "phone", dto.getPhone(), dto.getUserId())) {
                throw new DuplicateDataException("phone", "Số điện thoại đã được đăng ký!");
            }
            if (repo.isDuplicateInfo(conn, "identity_card", dto.getIdentityCard(), dto.getUserId())) {
                throw new DuplicateDataException("identityCard", "CCCD đã tồn tại trong hệ thống!");
            }

            boolean isUpdated = repo.updateUserProfile(conn, dto);
            if (!isUpdated) {
                conn.rollback();
                return Result.failure("Không tìm thấy người dùng để cập nhật.");
            }

            conn.commit(); 
            return Result.success(null, "Cập nhật hồ sơ thành công!");

        } catch (DuplicateDataException | IllegalArgumentException e) {
            rollbackTransaction(conn);
            throw e; 
        } catch (SQLException e) {
            rollbackTransaction(conn);
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi lưu dữ liệu.");
        } finally {
            closeConnection(conn);
        }
    }

    public Result<Void> changePassword(AccountUpdateDTO dto) {
        Connection conn = null;
        try {
            AccountValidator.validatePasswordChange(dto);
            
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            String currentHash = repo.getPasswordHash(conn, dto.getAccountId());
            if (currentHash == null) {
                return Result.failure("Tài khoản không tồn tại hoặc đã bị khóa.");
            }

            // Gọi đúng hàm checkPassword() của bạn trong PasswordUtil
            if (!PasswordUtil.checkPassword(dto.getOldPassword(), currentHash)) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
            }

            String newHash = PasswordUtil.hashPassword(dto.getNewPassword());
            boolean isUpdated = repo.updatePassword(conn, dto.getAccountId(), newHash);
            
            if (!isUpdated) {
                conn.rollback();
                return Result.failure("Không thể cập nhật mật khẩu lúc này.");
            }

            conn.commit();
            return Result.success(null, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");

        } catch (IllegalArgumentException e) {
            rollbackTransaction(conn);
            throw e; 
        } catch (SQLException e) {
            rollbackTransaction(conn);
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi đổi mật khẩu.");
        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================================
    // UTILITIES CHO TRANSACTION
    // =========================================================================

    private void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}