/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;

/**
 *
 * @author Tien Dat
 */
import java.sql.Date;

/**
 * Model quản lý sao lưu dữ liệu hệ thống
 */
public class SystemBackup {
    private int backupId;
    private String backupName;
    private String backupPath;
    private Date backupDate;
    private int createdBy;
    private String status;
    private String note;

    public SystemBackup() {
    }

    public SystemBackup(int backupId, String backupName, String backupPath, Date backupDate, int createdBy, String status, String note) {
        this.backupId = backupId;
        this.backupName = backupName;
        this.backupPath = backupPath;
        this.backupDate = backupDate;
        this.createdBy = createdBy;
        this.status = status;
        this.note = note;
    }

    public int getBackupId() { return backupId; }
    public void setBackupId(int backupId) { this.backupId = backupId; }

    public String getBackupName() { return backupName; }
    public void setBackupName(String backupName) { this.backupName = backupName; }

    public String getBackupPath() { return backupPath; }
    public void setBackupPath(String backupPath) { this.backupPath = backupPath; }

    public Date getBackupDate() { return backupDate; }
    public void setBackupDate(Date backupDate) { this.backupDate = backupDate; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "SystemBackup{" +
                "backupId=" + backupId +
                ", backupName='" + backupName + '\'' +
                ", backupPath='" + backupPath + '\'' +
                ", backupDate=" + backupDate +
                ", createdBy=" + createdBy +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
