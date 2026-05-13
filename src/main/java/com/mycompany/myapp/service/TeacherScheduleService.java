/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;
import com.mycompany.myapp.model.TeacherScheduleDTO;
import com.mycompany.myapp.repository.TeacherScheduleRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tien Dat
 */
public class TeacherScheduleService {
    private final TeacherScheduleRepository repository;
    
    public TeacherScheduleService(){
        this.repository = new TeacherScheduleRepository();
    }
    public TeacherScheduleService(TeacherScheduleRepository repository){
        this.repository = repository;
    }
    
    public Map<Integer, List<TeacherScheduleDTO>> getGroupedSchedule(Long teacherId){
        List<TeacherScheduleDTO> rawSchedule = repository.getStandardSchedule(teacherId);
        
        Map<Integer, List<TeacherScheduleDTO>> groupedSchedule = new HashMap<>();
        
        for(int i = 2; i <= 8; i++){
            groupedSchedule.put(i, new ArrayList<>());
        }
        
        for (TeacherScheduleDTO dto : rawSchedule){
           Integer day = dto.getDayOfWeek();
           
           if (day != null && day >= 2 && day <= 8){
               groupedSchedule.get(day).add(dto);
           } 
        }
        return groupedSchedule;
    }
    
}
