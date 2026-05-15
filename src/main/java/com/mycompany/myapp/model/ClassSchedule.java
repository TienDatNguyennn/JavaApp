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
 * Model quản lý lịch học của lớp
 */
public class ClassSchedule {
    private int scheduleId;
    private int classId;
    private int roomId;
    private int dayOfWeek;
    private String startTime;
    private String endTime;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

    public ClassSchedule() {
    }

    public ClassSchedule(int scheduleId, int classId, int roomId, int dayOfWeek, String startTime, String endTime, Date createdAt, Date updatedAt, boolean isDeleted) {
        this.scheduleId = scheduleId;
        this.classId = classId;
        this.roomId = roomId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "ClassSchedule{" +
                "scheduleId=" + scheduleId +
                ", classId=" + classId +
                ", roomId=" + roomId +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}