/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */
public class PermissionDTO {
    public int roleId;
    public int functionId;
    public String functionName;
    public boolean canView;
    public boolean canAdd;
    public boolean canEdit;
    public boolean canDelete;

    public PermissionDTO(int roleId, int functionId, String functionName, boolean canView, boolean canAdd, boolean canEdit, boolean canDelete) {
        this.roleId = roleId;
        this.functionId = functionId;
        this.functionName = functionName;
        this.canView = canView;
        this.canAdd = canAdd;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }
}