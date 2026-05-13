package com.mycompany.myapp.model;

public class AccountUpdateDTO {
    private int accountId;
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String identityCard;
    
    // Thuộc tính dùng riêng cho chức năng đổi mật khẩu
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    // Constructor rỗng mặc định
    public AccountUpdateDTO() {
    }

    // ================= GETTERS AND SETTERS =================

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}