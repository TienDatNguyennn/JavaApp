package com.mycompany.myapp.utils;

import com.mycompany.myapp.model.AccountUpdateDTO;

public class AccountValidator {
    
    public static void validateProfile(AccountUpdateDTO dto) {
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (dto.getEmail() == null || !dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        if (dto.getPhone() != null && !dto.getPhone().matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 số và bắt đầu bằng số 0.");
        }
        if (dto.getIdentityCard() != null && !dto.getIdentityCard().matches("^\\d{12}$")) {
            throw new IllegalArgumentException("CCCD phải đủ 12 số.");
        }
    }

    public static void validatePasswordChange(AccountUpdateDTO dto) {
        if (dto.getOldPassword() == null || dto.getOldPassword().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cũ.");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }
    }
}