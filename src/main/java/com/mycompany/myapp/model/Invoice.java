package com.mycompany.myapp.model;

import java.util.Date;

public class Invoice {
    private int invoiceId;
    private int studentId;
    private String studentName;
    private int staffId; // Thêm lại trường này
    private int promoId;
    private double totalAmount;
    private double discountAmt;
    private double finalAmount;
    private double amountPaid;
    private String paymentMethod;
    private String status;
    private String apiStatus; // Thêm lại trường này
    private Date createdAt;   // Thêm lại trường này

    public Invoice() {}

    // Getters & Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }
    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getDiscountAmt() { return discountAmt; }
    public void setDiscountAmt(double discountAmt) { this.discountAmt = discountAmt; }
    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApiStatus() { return apiStatus; }
    public void setApiStatus(String apiStatus) { this.apiStatus = apiStatus; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public double getDebtAmount() {
        return Math.max(0, this.finalAmount - this.amountPaid);
    }
}