package com.mycompany.myapp.model;

public class StaffOptionDTO {

    private int userId;
    private String fullName;
    private String staffType;
    private double baseSalary;

    public StaffOptionDTO() {
    }

    public StaffOptionDTO(int userId, String fullName, String staffType, double baseSalary) {
        this.userId = userId;
        this.fullName = fullName;
        this.staffType = staffType;
        this.baseSalary = baseSalary;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStaffType() {
        return staffType;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    @Override
    public String toString() {
        String typeDisplay = "TEACHER".equals(staffType) ? "Giáo viên" : "Nhân viên giáo vụ";
        return userId + " - " + fullName + " (" + typeDisplay + ")";
    }
}