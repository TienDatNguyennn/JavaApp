/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        User u1 = new User("U01", "namnd", "Nguyễn Duy Nam", "Active");
        u1.getAssignedRoles().add("MANAGE_VOLUNTEER"); // Giả lập quyền có sẵn
        
        users.add(u1);
        users.add(new User("U02", "dpv_linh", "Điều Phối Viên Linh", "Active"));
        users.add(new User("U03", "tnv_hoang", "Tình Nguyện Viên Hoàng", "Inactive"));
        users.add(new User("U04", "admin_hethong", "Quản Trị Hệ Thống", "Active"));
        return users;
    }
}