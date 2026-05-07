/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.controller;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.service.PermissionService;
import java.util.List;

public class PermissionController {
    private PermissionService service = new PermissionService();

    public List<PermissionDTO> getPermissions(int roleGroupId) throws Exception {
        return service.loadPermissions(roleGroupId);
    }

    public void updatePermissions(List<PermissionDTO> permissions) throws Exception {
        service.savePermissions(permissions);
    }
}