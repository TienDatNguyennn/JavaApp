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
public class TeacherScheduleDTO{
    private Long scheduleId;
    private Integer dayOfWeek;
    private String startTime;
    private String endTime;
    
    private Long classId;
    private String className;
    private String subjectName;
    private String roomName;
    private Date startDate;
    private Date endDate;
    
    // Constructor mặc định
    public TeacherScheduleDTO(){}
    
    //Constructor đầy đủ tham số giúp khởi tạo nhanh
    public TeacherScheduleDTO(Long scheduleId, Integer dayOfWeek, String startTime, String endTime, Long classId, String className, String subjectName, String roomName ){
        this.scheduleId = scheduleId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classId = classId;
        this.className = className;
        this.subjectName = subjectName;
        this.roomName = roomName;
    }
    
    //Getters and Setters
    
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public Date getStartDate() { 
        return startDate; 
    }
    
    public void setStartDate(Date startDate) { 
        this.startDate = startDate; 
    }

    public Date getEndDate() { 
        return endDate; 
    }
    
    public void setEndDate(Date endDate) { 
        this.endDate = endDate; 
    }
      
}