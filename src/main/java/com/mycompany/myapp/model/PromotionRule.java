package com.mycompany.myapp.model;

import java.sql.Date;

/** Model ánh xạ bảng PROMOTION_RULE. */
public class PromotionRule {
    private int    promoId;
    private String promoName;
    private double discountRate;   // 0–100 (%)
    private int    minSubjects;    // số môn tối thiểu để áp dụng
    private Date   createdAt;
    private Date   updatedAt;
    private boolean isDeleted;

    public PromotionRule() {}

    public int    getPromoId()      { return promoId; }
    public void   setPromoId(int v)      { promoId = v; }

    public String getPromoName()    { return promoName; }
    public void   setPromoName(String v) { promoName = v; }

    public double getDiscountRate() { return discountRate; }
    public void   setDiscountRate(double v) { discountRate = v; }

    public int    getMinSubjects()  { return minSubjects; }
    public void   setMinSubjects(int v)  { minSubjects = v; }

    public Date   getCreatedAt()    { return createdAt; }
    public void   setCreatedAt(Date v)   { createdAt = v; }

    public Date   getUpdatedAt()    { return updatedAt; }
    public void   setUpdatedAt(Date v)   { updatedAt = v; }

    public boolean isDeleted()      { return isDeleted; }
    public void   setDeleted(boolean v)  { isDeleted = v; }
}
