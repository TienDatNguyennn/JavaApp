package com.mycompany.myapp.utils;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    public interface AttendanceListener {
        void onAttendanceSuccess(int studentId, String status);
    }

    private static final List<AttendanceListener> listeners = new CopyOnWriteArrayList<>();

    private EventBus() {
    }

    public static void register(AttendanceListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void unregister(AttendanceListener listener) {
        listeners.remove(listener);
    }

    public static void publish(int studentId, String status) {
        for (AttendanceListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onAttendanceSuccess(studentId, status));
        }
    }
}