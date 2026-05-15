package com.mycompany.myapp.model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Tien Dat
 */

public class RoleGroup {
    private int id;
    private String name;

    public RoleGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    @Override
    public String toString() { return name; } // Để hiển thị đẹp trên JList
}