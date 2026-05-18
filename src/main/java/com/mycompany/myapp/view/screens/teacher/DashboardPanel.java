package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private JLabel lblTotalClasses, lblTotalStudents, lblTodaySchedules, lblPendingGrades;
    private DefaultTableModel scheduleModel, attendanceModel;
    private ModernTable scheduleTable, attendanceTable;
    private DashboardService service;

    public DashboardPanel() {
        service = new DashboardService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(245, 245, 249));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ================= TOP: 4 SUMMARY CARDS =================
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 110));

        lblTotalClasses = new JLabel("0", SwingConstants.LEFT);
        lblTotalStudents = new JLabel("0", SwingConstants.LEFT);
        lblTodaySchedules = new JLabel("0", SwingConstants.LEFT);
        lblPendingGrades = new JLabel("0", SwingConstants.LEFT);

        cardsPanel.add(createCard("Lớp Đang Dạy", lblTotalClasses, "Lớp", new Color(99, 102, 241)));
        cardsPanel.add(createCard("Tổng Học Viên", lblTotalStudents, "HV", new Color(99, 102, 241)));
        cardsPanel.add(createCard("Lịch Dạy Hôm Nay", lblTodaySchedules, "Ca", new Color(99, 102, 241)));
        cardsPanel.add(createCard("Chờ Chấm Điểm", lblPendingGrades, "Bài", new Color(99, 102, 241)));

        add(cardsPanel, BorderLayout.NORTH);

        // ================= CENTER: 2 BẢNG THỐNG KÊ CHÍNH =================
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 25, 0));
        mainContent.setOpaque(false);

        // PANEL 1: LỊCH DẠY SẮP TỚI
        RoundedPanel scheduleContainer = new RoundedPanel(15);
        scheduleContainer.setBackground(Color.WHITE);
        scheduleContainer.setLayout(new BorderLayout(0, 10));
        scheduleContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblScheduleTitle = new JLabel("Lịch Dạy Sắp Tới");
        lblScheduleTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scheduleContainer.add(lblScheduleTitle, BorderLayout.NORTH);

        String[] schCols = {"Thứ", "Thời Gian", "Lớp", "Môn Học"};
        scheduleModel = new DefaultTableModel(schCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        scheduleTable = new ModernTable();
        scheduleTable.setModel(scheduleModel);
        scheduleTable.setRowHeight(35);
        scheduleContainer.add(new ModernScrollPane(scheduleTable), BorderLayout.CENTER);

        // PANEL 2: THỐNG KÊ CHUYÊN CẦN
        RoundedPanel attendanceContainer = new RoundedPanel(15);
        attendanceContainer.setBackground(Color.WHITE);
        attendanceContainer.setLayout(new BorderLayout(0, 10));
        attendanceContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblAttTitle = new JLabel("Thống Kê Chuyên Cần");
        lblAttTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        attendanceContainer.add(lblAttTitle, BorderLayout.NORTH);

        String[] attCols = {"Tên Lớp", "Tỷ lệ đi học trung bình"};
        attendanceModel = new DefaultTableModel(attCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        attendanceTable = new ModernTable();
        attendanceTable.setModel(attendanceModel);
        attendanceTable.setRowHeight(35);

        // Render màu sắc cho tỷ lệ
        attendanceTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                try {
                    double rate = Double.parseDouble(v.toString().replace("%", "").trim());
                    if (rate < 80) comp.setForeground(new Color(239, 68, 68)); // Đỏ
                    else comp.setForeground(new Color(34, 197, 94)); // Xanh
                } catch (Exception e) {}
                if (isSel) comp.setForeground(Color.WHITE);
                return comp;
            }
        });
        attendanceContainer.add(new ModernScrollPane(attendanceTable), BorderLayout.CENTER);

        mainContent.add(scheduleContainer);
        mainContent.add(attendanceContainer);

        add(mainContent, BorderLayout.CENTER);
    }

    // Helper tạo thẻ Card giống thiết kế của bạn
    private JPanel createCard(String title, JLabel valueLabel, String unit, Color accentColor) {
        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(107, 114, 128));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valuePanel.setOpaque(false);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accentColor);
        
        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUnit.setForeground(accentColor);
        
        valuePanel.add(valueLabel);
        valuePanel.add(lblUnit);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        SwingWorker<Result<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<Map<String, Object>> doInBackground() {
                return service.getFullDashboardData();
            }

            @Override
            protected void done() {
                try {
                    Result<Map<String, Object>> res = get();
                    if (res.isSuccess()) {
                        Map<String, Object> data = res.getData();

                        // 1. Set Metrics
                        Map<String, Integer> metrics = (Map<String, Integer>) data.get("metrics");
                        lblTotalClasses.setText(String.valueOf(metrics.get("totalClasses")));
                        lblTotalStudents.setText(String.valueOf(metrics.get("totalStudents")));
                        lblTodaySchedules.setText(String.valueOf(metrics.get("todaySchedules")));
                        lblPendingGrades.setText(String.valueOf(metrics.get("pendingGrades")));

                        // 2. Load Schedules
                        scheduleModel.setRowCount(0);
                        List<Map<String, Object>> schedules = (List<Map<String, Object>>) data.get("schedules");
                        for (Map<String, Object> s : schedules) {
                            scheduleModel.addRow(new Object[]{ s.get("day"), s.get("time"), s.get("class_name"), s.get("subject") });
                        }

                        // 3. Load Attendance
                        attendanceModel.setRowCount(0);
                        List<Map<String, Object>> attendance = (List<Map<String, Object>>) data.get("attendance");
                        for (Map<String, Object> a : attendance) {
                            attendanceModel.addRow(new Object[]{ a.get("class_name"), String.format("%.1f %%", (Double) a.get("rate")) });
                        }
                    } else {
                        JOptionPane.showMessageDialog(DashboardPanel.this, res.getMessage());
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }
}