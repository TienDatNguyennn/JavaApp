package com.mycompany.myapp.service;

import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.repository.PermissionRepository;

import java.sql.SQLException;
import java.util.List;

public class PermissionService {

    private final PermissionRepository repo = new PermissionRepository();

    public List<PermissionDTO> loadPermissions(int roleGroupId) throws SQLException {
        if (roleGroupId <= 0) {
            throw new SQLException("roleGroupId không hợp lệ.");
        }

        return repo.getPermissionsByRoleGroup(roleGroupId);
    }

    /*
     * Hàm cũ, giữ lại để code cũ không lỗi.
     * Chỉ dùng được khi tất cả PermissionDTO đều đã có roleId.
     */
    public void savePermissions(List<PermissionDTO> permissions) throws SQLException {
        if (permissions == null || permissions.isEmpty()) {
            throw new SQLException("Danh sách quyền trống.");
        }

        repo.updatePermissions(permissions);
    }

    /*
     * Hàm chuẩn để lưu quyền nhóm.
     * AccountManagerUI nên gọi hàm này khi chọn "Theo Nhóm Quyền".
     */
    public void updateRoleGroupPermissions(int roleGroupId, List<PermissionDTO> permissions) throws SQLException {
        if (roleGroupId <= 0) {
            throw new SQLException("Không xác định được nhóm quyền cần lưu.");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new SQLException("Danh sách quyền trống.");
        }

        repo.updateRoleGroupPermissions(roleGroupId, permissions);
    }

    public List<PermissionDTO> getPermissionsByAccount(int accountId) throws SQLException {
        if (accountId <= 0) {
            throw new SQLException("accountId không hợp lệ.");
        }

        return repo.getPermissionsByAccount(accountId);
    }

    /*
     * Hàm chuẩn để lưu quyền riêng theo tài khoản.
     * AccountManagerUI phải gọi hàm này khi chọn "Theo Tài Khoản".
     */
    public void updateAccountCustomPermissions(int accountId, List<PermissionDTO> permissions) throws SQLException {
        if (accountId <= 0) {
            throw new SQLException("Không xác định được tài khoản cần phân quyền.");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new SQLException("Danh sách quyền trống.");
        }

        repo.saveAccountCustomPermissions(accountId, permissions);
    }
}