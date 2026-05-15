package com.mycompany.myapp.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Tien Dat
 */

import com.mycompany.myapp.model.AccountDTO;
import com.mycompany.myapp.model.FunctionPermission;
import com.mycompany.myapp.model.RoleGroup;
import java.util.ArrayList;
import java.util.List;

public class PermissionService {
    
    public List<AccountDTO> getAllAccounts() {
        List<AccountDTO> list = new ArrayList<>();
        AccountDTO acc1 = new AccountDTO(1, "admin_hethong", "Nguyễn Văn A", "ACTIVE");
        acc1.getAssignedGroupIds().add(3); // Giả lập thuộc nhóm QL Hệ thống
        list.add(acc1);
        list.add(new AccountDTO(2, "gv_tranb", "Trần Thị B (Giáo viên)", "ACTIVE"));
        list.add(new AccountDTO(3, "nv_nghiepvu", "Lê Văn C (Nghiệp vụ)", "ACTIVE"));
        list.add(new AccountDTO(4, "kt_ketoan", "Phạm Thị D (Kế toán)", "LOCKED"));
        return list;
    }

    public List<RoleGroup> getActorRoleGroups() {
        List<RoleGroup> groups = new ArrayList<>();
        groups.add(new RoleGroup(1, "Nhân viên quản lý nghiệp vụ"));
        groups.add(new RoleGroup(2, "Giáo viên"));
        groups.add(new RoleGroup(3, "Nhân viên quản lý hệ thống"));
        groups.add(new RoleGroup(4, "Nhân viên kế toán"));
        return groups;
    }

    public List<FunctionPermission> getAllFunctionsForMatrix() {
        List<FunctionPermission> funcs = new ArrayList<>();
        funcs.add(new FunctionPermission(101, "Quản lý Khóa học & Lớp học", false, false, false, false));
        funcs.add(new FunctionPermission(102, "Quản lý Lịch biểu & Điểm danh", false, false, false, false));
        funcs.add(new FunctionPermission(103, "Quản lý Học viên & Điểm số", false, false, false, false));
        funcs.add(new FunctionPermission(104, "Quản lý Hóa đơn & Học phí", false, false, false, false));
        funcs.add(new FunctionPermission(105, "Quản trị Hệ thống & Phân quyền", false, false, false, false));
        return funcs;
    }

    public void saveGroupAssignment(int accountId, List<Integer> groupIds) throws Exception {
        Thread.sleep(600); // Giả lập lưu DB vào bảng ACCOUNT_ASSIGN_ROLE_GROUP
        System.out.println("[DB] Đã cập nhật Nhóm Quyền cho Account ID: " + accountId);
    }

    public void saveMatrixPermissions(int accountId, List<FunctionPermission> perms) throws Exception {
        Thread.sleep(600); // Giả lập insert vào bảng ROLE và ACCOUNT_ASSIGN_ROLE
        System.out.println("[DB] Đã cập nhật Quyền lẻ (Matrix) cho Account ID: " + accountId);
    }
}