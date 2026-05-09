package com.mycompany.myapp.model;

import java.util.Date;

/**
 * Model ánh xạ bảng INVOICE trong database
 */
public class Invoice {
    private int    invoiceId;
    private int    studentId;
    private String studentName;    // JOIN từ USERS.full_name
    private int    staffId;
    private double totalAmount;
    private double discountAmt;
    private double finalAmount;
    private double amountPaid;
    private String paymentMethod;
    private String status;         // PAID | PARTIAL | UNPAID
    private String apiStatus;      // SENT | PENDING | ERROR | ADJUSTED
    private Date   createdAt;

    // ── Getters & Setters ──────────────────────────────────────────
    public int    getInvoiceId()        { return invoiceId; }
    public void   setInvoiceId(int v)   { this.invoiceId = v; }

    public int    getStudentId()        { return studentId; }
    public void   setStudentId(int v)   { this.studentId = v; }

    public String getStudentName()          { return studentName; }
    public void   setStudentName(String v)  { this.studentName = v; }

    public int    getStaffId()          { return staffId; }
    public void   setStaffId(int v)     { this.staffId = v; }

    public double getTotalAmount()       { return totalAmount; }
    public void   setTotalAmount(double v){ this.totalAmount = v; }

    public double getDiscountAmt()       { return discountAmt; }
    public void   setDiscountAmt(double v){ this.discountAmt = v; }

    public double getFinalAmount()       { return finalAmount; }
    public void   setFinalAmount(double v){ this.finalAmount = v; }

    public double getAmountPaid()        { return amountPaid; }
    public void   setAmountPaid(double v){ this.amountPaid = v; }

    public String getPaymentMethod()         { return paymentMethod; }
    public void   setPaymentMethod(String v) { this.paymentMethod = v; }

    public String getStatus()         { return status; }
    public void   setStatus(String v) { this.status = v; }

    public String getApiStatus()         { return apiStatus; }
    public void   setApiStatus(String v) { this.apiStatus = v; }

    public Date   getCreatedAt()        { return createdAt; }
    public void   setCreatedAt(Date v)  { this.createdAt = v; }

    /** Tiện ích: số tiền còn nợ */
    public double getDebtAmount() {
        return Math.max(0, finalAmount - amountPaid);
    }
}