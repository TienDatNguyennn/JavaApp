package com.mycompany.myapp.model;

import java.util.ArrayList;
import java.util.List;

public class InvoiceEmailDTO {

    public int invoiceId;
    public String studentName;
    public String parentName;
    public String parentPhone;
    public String paymentMethod;
    public String status;
    public String apiStatus;

    public double totalAmount;
    public double discountAmount;
    public double finalAmount;

    public List<InvoiceEmailItemDTO> items = new ArrayList<>();

    public InvoiceEmailDTO() {
    }
}