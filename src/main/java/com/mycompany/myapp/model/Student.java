package com.mycompany.myapp.model;

import java.sql.Date;

public class Student {
    private int studentId;
    private String fullName;
    private Date dob;
    private String gender;
    private String phone;
    private String parentName;
    private String parentPhone;
    private String address;
    
    // Đã chuyển từ int sang Integer để hỗ trợ giá trị null từ Database
    private Integer managedBy; 
    
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public Student() {
    }

    public Student(int studentId, String fullName, Date dob, String gender, String phone, String parentName, String parentPhone, String address, Integer managedBy, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.address = address;
        this.managedBy = managedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getManagedBy() { return managedBy; }
    public void setManagedBy(Integer managedBy) { this.managedBy = managedBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
}