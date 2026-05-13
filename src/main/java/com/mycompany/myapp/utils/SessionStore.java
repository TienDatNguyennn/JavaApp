/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp quản lý phiên làm việc (Session) của ứng dụng.
 * Áp dụng tiêu chuẩn Utility Class, Thread-Safe và Immutable Data.
 */
public class SessionStore {
    
    // 1. Dùng từ khóa 'volatile' để đảm bảo tính đồng nhất dữ liệu khi ứng dụng chạy nhiều luồng (Ví dụ: SwingWorker)
    private static volatile String currentToken;
    private static volatile Map<String, Object> userInfo;
    private static volatile List<String> roles;

    // 2. Private constructor: Tiêu chuẩn Clean Code cho các Utility Class (class chỉ chứa static method)
    // Ngăn chặn việc dev khác vô tình tạo đối tượng: new SessionStore()
    private SessionStore() {
        throw new IllegalStateException("Utility class - Không được phép khởi tạo");
    }

    // 3. Dùng synchronized để tránh xung đột nếu có 2 luồng cùng ghi đè session
    public static synchronized void saveSession(String token, Map<String, Object> user, List<String> userRoles) {
        currentToken = token;
        
        // 4. IMMUTABILITY (Bất biến): Bọc dữ liệu bằng unmodifiable
        // Tránh lỗi bảo mật nghiêm trọng: Một class nào đó gọi SessionStore.getUserRoles().add("Admin")
        userInfo = user != null ? Collections.unmodifiableMap(new HashMap<>(user)) : null;
        roles = userRoles != null ? Collections.unmodifiableList(new ArrayList<>(userRoles)) : null;
    }

    public static String getCurrentToken() { 
        return currentToken; 
    }
    
    // Hàm mới: Kiểm tra xem user đã đăng nhập chưa (rất hay dùng khi check chuyển trang)
    public static boolean isAuthenticated() {
        return currentToken != null && userInfo != null;
    }

    public static String getFullName() {
        // Parse dữ liệu an toàn, tránh NullPointerException
        if (userInfo != null && userInfo.get("full_name") != null) {
            return String.valueOf(userInfo.get("full_name"));
        }
        return "Người dùng";
    }
    
    // Hàm mới: Lấy ID tài khoản để thực hiện các câu lệnh truy vấn SQL (Update, Delete chính mình...)
    public static Integer getAccountId() {
        if (userInfo != null && userInfo.get("account_id") != null) {
            return (Integer) userInfo.get("account_id");
        }
        return -1; 
    }

    public static boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }
    
    // Hàm mới: Trả về danh sách quyền (Đảm bảo trả về list rỗng nếu null để tránh lỗi NullPointerException khi dùng vòng lặp)
    public static List<String> getUserRoles() {
        return roles != null ? roles : Collections.emptyList();
    }

    public static synchronized void clearSession() {
        currentToken = null;
        userInfo = null;
        roles = null;
    }
    
    // Thêm vào SessionStore.java
    public static Integer getUserId() {
        if (userInfo != null && userInfo.get("user_id") != null) {
            return (Integer) userInfo.get("user_id");
        }
        return -1; 
    }
}