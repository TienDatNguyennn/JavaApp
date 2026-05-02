/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */

public class Role {
    private String code;
    private String description;

    public Role(String code, String description) {
        this.code = code;
        this.description = description;
    }
    public String getCode() { return code; }
    public String getDescription() { return description; }
}