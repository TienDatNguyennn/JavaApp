package com.mycompany.myapp.model;

/**
 * Model ánh xạ bảng PAYROLL trong database
 */
public class Payroll {
    private int    payrollId;
    private int    userId;
    private String fullName;         // JOIN từ USERS.full_name
    private String staffType;        // TEACHER | OFFICE
    private String payPeriod;        // Định dạng "YYYY-MM", ví dụ "2025-05"
    private double totalTeachingFee; // Phí giảng dạy (chỉ áp dụng TEACHER)
    private double basicSalary;      // Lương cơ bản
    private double bonusAmount;      // Thưởng
    private double totalNet;         // Tổng thực lĩnh (tự tính = basic + teaching + bonus)

    // ── Getters & Setters ──────────────────────────────────────────
    public int    getPayrollId()        { return payrollId; }
    public void   setPayrollId(int v)   { this.payrollId = v; }

    public int    getUserId()           { return userId; }
    public void   setUserId(int v)      { this.userId = v; }

    public String getFullName()         { return fullName; }
    public void   setFullName(String v) { this.fullName = v; }

    public String getStaffType()         { return staffType; }
    public void   setStaffType(String v) { this.staffType = v; }

    public String getPayPeriod()         { return payPeriod; }
    public void   setPayPeriod(String v) { this.payPeriod = v; }

    public double getTotalTeachingFee()        { return totalTeachingFee; }
    public void   setTotalTeachingFee(double v){ this.totalTeachingFee = v; }

    public double getBasicSalary()        { return basicSalary; }
    public void   setBasicSalary(double v){ this.basicSalary = v; }

    public double getBonusAmount()        { return bonusAmount; }
    public void   setBonusAmount(double v){ this.bonusAmount = v; }

    public double getTotalNet()        { return totalNet; }
    public void   setTotalNet(double v){ this.totalNet = v; }

    /** Tự tính lại tổng net từ 3 thành phần */
    public void recalcTotalNet() {
        this.totalNet = this.basicSalary + this.totalTeachingFee + this.bonusAmount;
    }
}