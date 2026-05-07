/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */
public class AccountListDTO {
    public int accountId;
    public String username;
    public String fullName;
    public String email;
    public String roleGroupName;
    public String status;

    public AccountListDTO(int accountId, String username, String fullName, String email, String roleGroupName, String status) {
        this.accountId = accountId; this.username = username; this.fullName = fullName;
        this.email = email; this.roleGroupName = roleGroupName; this.status = status;
    }
}