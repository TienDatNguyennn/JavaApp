package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.service.AttendanceConcurrencyService;
import com.mycompany.myapp.utils.EventBus;
import com.mycompany.myapp.utils.SecurityUtils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class MockConcurrencyTestUI extends JPanel {

    private JButton btnMassTest;
    private JButton btnSpamTest;
    private JTextArea logArea;

    private final AttendanceConcurrencyService concurrencyService;

    /*
     * Phải là schedule_id có thật trong CLASS_SCHEDULE.
     * Đồng thời học viên test phải thuộc class_id của schedule này.
     */
    private final int testScheduleId = 1;

    public MockConcurrencyTestUI() {
        concurrencyService = new AttendanceConcurrencyService();
        initComponents();
        registerEventBus();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnMassTest = new JButton("1. Test 50 học viên quét cùng lúc");
        btnSpamTest = new JButton("2. Test 1 học viên spam 10 lần");

        topPanel.add(btnMassTest);
        topPanel.add(btnSpamTest);

        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        add(new JScrollPane(logArea), BorderLayout.CENTER);

        btnMassTest.addActionListener(e -> runMassTest());
        btnSpamTest.addActionListener(e -> runSpamTest());
    }

    private void registerEventBus() {
        EventBus.register((studentId, status) -> {
            log("[EVENT BUS] studentId=" + studentId + " | status=" + status);
        });
    }

    private void runMassTest() {
        log("==================================================");
        log("KỊCH BẢN 1: 50 HỌC VIÊN QUÉT QR GẦN NHƯ ĐỒNG THỜI");
        log("schedule_id = " + testScheduleId);
        log("Điều kiện đúng: student_id 1..50 phải thuộc lớp của schedule này.");
        log("==================================================");

        String validToken = generateMockToken();

        for (int i = 1; i <= 50; i++) {
            concurrencyService.submitScanRequest(i, testScheduleId, validToken);
        }
    }

    private void runSpamTest() {
        log("==================================================");
        log("KỊCH BẢN 2: 1 HỌC VIÊN SPAM 10 REQUEST CÙNG LÚC");
        log("Kỳ vọng: 1 request thành công, 9 request bị chặn DUPLICATE.");
        log("==================================================");

        String validToken = generateMockToken();

        int spamStudentId = 1;
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    latch.await();
                    concurrencyService.submitScanRequest(spamStudentId, testScheduleId, validToken);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }, "SPAM-REQ-" + i).start();
        }

        latch.countDown();
    }

    private String generateMockToken() {
        String token = "TOKEN_LIVE_TEST";
        long exp = System.currentTimeMillis() + 60000;

        String payload = testScheduleId + "|" + token + "|" + exp;
        return payload + "|" + SecurityUtils.generateHash(payload);
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EduFlex - Giả lập điểm danh QR đồng thời");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(780, 520);
            frame.setLocationRelativeTo(null);
            frame.add(new MockConcurrencyTestUI());
            frame.setVisible(true);
        });
    }
}