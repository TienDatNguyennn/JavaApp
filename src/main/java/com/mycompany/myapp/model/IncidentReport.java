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
 * Model báo cáo sự cố hệ thống
 */
public class IncidentReport {
    private int incidentId;
    private String title;
    private String description;
    private int reportedBy;
    private Date reportedDate;
    private String severity;
    private String status;
    private String solution;

    public IncidentReport() {
    }

    public IncidentReport(int incidentId, String title, String description, int reportedBy, Date reportedDate, String severity, String status, String solution) {
        this.incidentId = incidentId;
        this.title = title;
        this.description = description;
        this.reportedBy = reportedBy;
        this.reportedDate = reportedDate;
        this.severity = severity;
        this.status = status;
        this.solution = solution;
    }

    public int getIncidentId() { return incidentId; }
    public void setIncidentId(int incidentId) { this.incidentId = incidentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getReportedBy() { return reportedBy; }
    public void setReportedBy(int reportedBy) { this.reportedBy = reportedBy; }

    public Date getReportedDate() { return reportedDate; }
    public void setReportedDate(Date reportedDate) { this.reportedDate = reportedDate; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }

    @Override
    public String toString() {
        return "IncidentReport{" +
                "incidentId=" + incidentId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", reportedBy=" + reportedBy +
                ", reportedDate=" + reportedDate +
                ", severity='" + severity + '\'' +
                ", status='" + status + '\'' +
                ", solution='" + solution + '\'' +
                '}';
    }
}
