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
 * Data Transfer Object cho Thời khóa biểu của giáo viên
 */
public class TeacherScheduleDTO {
    private int scheduleId;
    private String className;
    private String subjectName;
    private String roomName;
    private String teacherName;
    private int dayOfWeek;
    private String startTime;
    private String endTime;
    private Date startDate;
    private Date endDate;

    public TeacherScheduleDTO() {
    }

    public TeacherScheduleDTO(int scheduleId, String className, String subjectName, String roomName, String teacherName, int dayOfWeek, String startTime, String endTime, Date startDate, Date endDate) {
        this.scheduleId = scheduleId;
        this.className = className;
        this.subjectName = subjectName;
        this.roomName = roomName;
        this.teacherName = teacherName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return "TeacherScheduleDTO{" +
                "scheduleId=" + scheduleId +
                ", className='" + className + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", roomName='" + roomName + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
