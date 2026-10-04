package com.skylinecrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_leads_mobile", columnList = "mobile"),
        @Index(name = "idx_leads_status", columnList = "qualification_status")
})
public class LeadEntity {
    @Id
    @Column(length = 100)
    private String id;
    @Column(name = "customer_name", length = 500)
    private String customerName;
    @Column(length = 500)
    private String name;
    @Column(length = 500)
    private String mobile;
    @Column(length = 500)
    private String email;
    @Column(name = "qualification_status", length = 500)
    private String qualificationStatus;
    @Column(length = 500)
    private String status;
    private BigDecimal budget;
    @Column(name = "loan_required")
    private Boolean loanRequired;
    @Column(name = "created_at", length = 64)
    private String createdAt;
    @Column(name = "updated_at", length = 64)
    private String updatedAt;

    public LeadEntity() {}
    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { customerName = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getMobile() { return mobile; } public void setMobile(String v) { mobile = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getQualificationStatus() { return qualificationStatus; } public void setQualificationStatus(String v) { qualificationStatus = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public BigDecimal getBudget() { return budget; } public void setBudget(BigDecimal v) { budget = v; }
    public Boolean getLoanRequired() { return loanRequired; } public void setLoanRequired(Boolean v) { loanRequired = v; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String v) { createdAt = v; }
    public String getUpdatedAt() { return updatedAt; } public void setUpdatedAt(String v) { updatedAt = v; }
}
