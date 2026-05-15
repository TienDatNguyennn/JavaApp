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

public class User {
    private String id;
    private String username;
    private String fullName;
    private String status;
    private Set<String> assignedRoles = new HashSet<>();
    private Set<String> assignedGroups = new HashSet<>();

    public User(String id, String username, String fullName, String status) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public Set<String> getAssignedRoles() { return assignedRoles; }
    public Set<String> getAssignedGroups() { return assignedGroups; }

    public void setAssignedRoles(Set<String> roles) { this.assignedRoles = roles; }
    public void setAssignedGroups(Set<String> groups) { this.assignedGroups = groups; }
}