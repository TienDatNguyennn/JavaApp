package com.mycompany.myapp.service;

import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.repository.PermissionRepository;
import java.sql.SQLException;
import java.util.List;

public class PermissionService {
    private PermissionRepository repo = new PermissionRepository();

    public List<PermissionDTO> loadPermissions(int roleGroupId) throws SQLException {
        return repo.getPermissionsByRoleGroup(roleGroupId);
    }

    public void savePermissions(List<PermissionDTO> permissions) throws SQLException {
        if (permissions == null || permissions.isEmpty()) return;
        repo.updatePermissions(permissions);
    }

    public List<PermissionDTO> getPermissionsByAccount(int accountId) throws SQLException {
        return repo.getPermissionsByAccount(accountId);
    }

    public void updateAccountCustomPermissions(int accountId, List<PermissionDTO> permissions) throws SQLException {
        if (permissions == null || permissions.isEmpty()) return;
        repo.saveAccountCustomPermissions(accountId, permissions);
    }
}