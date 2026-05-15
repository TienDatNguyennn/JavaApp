package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.service.PermissionService;
import java.sql.SQLException;
import java.util.List;

public class PermissionController {
    private PermissionService service = new PermissionService();

    public List<PermissionDTO> getPermissions(int roleGroupId) throws SQLException {
        return service.loadPermissions(roleGroupId);
    }

    public void updatePermissions(List<PermissionDTO> permissions) throws SQLException {
        service.savePermissions(permissions);
    }

    public List<PermissionDTO> getPermissionsByAccount(int accountId) throws SQLException {
        return service.getPermissionsByAccount(accountId);
    }

    public void updateAccountCustomPermissions(int accountId, List<PermissionDTO> permissions) throws SQLException {
        service.updateAccountCustomPermissions(accountId, permissions);
    }
}