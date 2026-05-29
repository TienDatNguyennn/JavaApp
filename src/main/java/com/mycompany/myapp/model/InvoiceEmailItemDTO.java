package com.mycompany.myapp.model;

public class InvoiceEmailItemDTO {

    public String className;
    public String subjectName;
    public double amount;

    public InvoiceEmailItemDTO() {
    }

    public InvoiceEmailItemDTO(String className, String subjectName, double amount) {
        this.className = className;
        this.subjectName = subjectName;
        this.amount = amount;
    }
}