/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */
public class RoleGroup {
    private int roleGroupId;
    private String nameRoleGroup;

    public RoleGroup(int roleGroupId, String nameRoleGroup) {
        this.roleGroupId = roleGroupId;
        this.nameRoleGroup = nameRoleGroup;
    }
    public int getRoleGroupId() { return roleGroupId; }
    public String getNameRoleGroup() { return nameRoleGroup; }
    @Override public String toString() { return nameRoleGroup; }
}