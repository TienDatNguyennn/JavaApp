package com.mycompany.myapp.utils;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    public interface AttendanceListener {
        void onAttendanceSuccess(int studentId, String status);
    }

    private static final List<AttendanceListener> listeners = new ArrayList<>();

    public static synchronized void register(AttendanceListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    // Phải là public static synchronized void
    public static synchronized void publish(int studentId, String status) {
        for (AttendanceListener listener : listeners) {
            listener.onAttendanceSuccess(studentId, status);
        }
    }
}