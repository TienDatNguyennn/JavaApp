/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

/**
 *
 * @author Tien Dat
 */
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
}