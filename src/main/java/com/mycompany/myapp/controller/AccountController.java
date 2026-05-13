package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.service.AccountService;
import com.mycompany.myapp.exception.DuplicateDataException;
import java.util.List;

public class AccountController {
    private AccountService service = new AccountService();

    public List<RoleGroup> getAvailableRoles() throws Exception { 
        return service.getRoles(); 
    }
    
    // [ĐÃ SỬA]: Chuyển thành lấy danh sách theo bộ lọc
    public List<AccountListDTO> getAccountsByFilter(String filter) throws Exception { 
        return service.getAccountsByFilter(filter); 
    }
    
    public void deleteAccount(int accountId) throws Exception { 
        service.deleteAccount(accountId); 
    }
    
    // [THÊM MỚI]: Khôi phục tài khoản
    public void restoreAccount(int accountId) throws Exception { 
        service.restoreAccount(accountId); 
    }

    public void handleCreateAccount(String fullName, String email, String phone, String username, String password, RoleGroup roleGroup, String status) throws Exception {
        // 1. Validation cơ bản
        if (username.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty() || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ các thông tin bắt buộc!");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu cấp phát phải từ 6 ký tự trở lên!");
        }
        if (phone != null && !phone.isEmpty() && !phone.startsWith("0")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        // 2. Kiểm tra trùng lặp dữ liệu qua Service
        if (service.isEmailExists(email)) {
            throw new DuplicateDataException("email", "Email '" + email + "' đã được sử dụng. Vui lòng nhập email khác!");
        }
        if (service.isPhoneExists(phone)) {
            throw new DuplicateDataException("phone", "Số điện thoại '" + phone + "' đã tồn tại trong hệ thống!");
        }
        if (service.isUsernameExists(username)) {
            throw new DuplicateDataException("username", "Tên đăng nhập '" + username + "' đã có người sử dụng!");
        }

        // 3. Thực thi transaction cấp phát tài khoản
        service.createAccountTransaction(fullName, email, phone, username, password, roleGroup.getRoleGroupId(), status);
    }
}