package com.mycompany.myapp.service;

import com.mycompany.myapp.model.TeacherScheduleDTO;
import com.mycompany.myapp.repository.TeacherScheduleRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý thời khóa biểu giáo viên.
 * 
 * Nhiệm vụ:
 * - Gọi repository để lấy lịch dạy từ database.
 * - Gom lịch dạy theo thứ trong tuần.
 * - Tạo sẵn key từ Thứ 2 đến Chủ Nhật để UI luôn hiển thị đủ 7 ngày.
 */
public class TeacherScheduleService {

    private final TeacherScheduleRepository repository;

    public TeacherScheduleService() {
        this.repository = new TeacherScheduleRepository();
    }

    public TeacherScheduleService(TeacherScheduleRepository repository) {
        this.repository = repository;
    }

    public Map<Integer, List<TeacherScheduleDTO>> getGroupedSchedule(Long teacherId) {
        Map<Integer, List<TeacherScheduleDTO>> groupedSchedule = new HashMap<>();

        for (int i = 2; i <= 8; i++) {
            groupedSchedule.put(i, new ArrayList<>());
        }

        if (teacherId == null || teacherId <= 0) {
            return groupedSchedule;
        }

        List<TeacherScheduleDTO> rawSchedule = repository.getStandardSchedule(teacherId);

        for (TeacherScheduleDTO dto : rawSchedule) {
            if (dto == null || dto.getDayOfWeek() == null) {
                continue;
            }

            Integer day = dto.getDayOfWeek();

            if (day >= 2 && day <= 8) {
                groupedSchedule.get(day).add(dto);
            }
        }

        return groupedSchedule;
    }
}