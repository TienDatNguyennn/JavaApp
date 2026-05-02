/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */
import java.util.HashSet;
import java.util.Set;

public class AccountDTO {
    private int accountId;
    private String username;
    private String fullName;
    private String status;
    private Set<Integer> assignedGroupIds = new HashSet<>();
    // Lưu trữ quyền lẻ dưới dạng danh sách các FunctionPermission
    private java.util.List<FunctionPermission> customPermissions = new java.util.ArrayList<>();

    public AccountDTO(int accountId, String username, String fullName, String status) {
        this.accountId = accountId;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
    }

    public int getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public Set<Integer> getAssignedGroupIds() { return assignedGroupIds; }
    public java.util.List<FunctionPermission> getCustomPermissions() { return customPermissions; }
}