package com.mycompany.myapp.controller;

import com.mycompany.myapp.exception.DuplicateDataException;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.service.AccountService;

import java.util.List;

public class AccountController {

    private final AccountService service = new AccountService();

    public List<RoleGroup> getAvailableRoles() throws Exception {
        return service.getRoles();
    }

    public List<AccountListDTO> getAccountsByFilter(String filter) throws Exception {
        return service.getAccountsByFilter(filter);
    }

    public void deleteAccount(int accountId) throws Exception {
        if (accountId <= 0) {
            throw new IllegalArgumentException("Không xác định được tài khoản cần khóa.");
        }

        service.deleteAccount(accountId);
    }

    public void restoreAccount(int accountId) throws Exception {
        if (accountId <= 0) {
            throw new IllegalArgumentException("Không xác định được tài khoản cần mở khóa.");
        }

        service.restoreAccount(accountId);
    }

    public void handleCreateAccount(
            String fullName,
            String email,
            String phone,
            String username,
            String password,
            RoleGroup roleGroup,
            String status
    ) throws Exception {

        fullName = normalize(fullName);
        email = normalize(email);
        phone = normalize(phone);
        username = normalize(username);
        password = password == null ? "" : password.trim();
        status = normalize(status);

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ các thông tin bắt buộc!");
        }

        if (roleGroup == null || roleGroup.getRoleGroupId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nhóm quyền cho tài khoản!");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu cấp phát phải từ 6 ký tự trở lên!");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }

        if (!phone.isEmpty() && !phone.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ. Số điện thoại phải bắt đầu bằng 0 và có 10 số!");
        }

        if (status.isEmpty()) {
            status = "ACTIVE";
        }

        if (service.isEmailExists(email)) {
            throw new DuplicateDataException(
                    "email",
                    "Email '" + email + "' đã được sử dụng. Vui lòng nhập email khác!"
            );
        }

        if (!phone.isEmpty() && service.isPhoneExists(phone)) {
            throw new DuplicateDataException(
                    "phone",
                    "Số điện thoại '" + phone + "' đã tồn tại trong hệ thống!"
            );
        }

        if (service.isUsernameExists(username)) {
            throw new DuplicateDataException(
                    "username",
                    "Tên đăng nhập '" + username + "' đã có người sử dụng!"
            );
        }

        service.createAccountTransaction(
                fullName,
                email,
                phone,
                username,
                password,
                roleGroup.getRoleGroupId(),
                status
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}