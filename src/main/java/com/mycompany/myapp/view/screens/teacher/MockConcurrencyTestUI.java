package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.service.AttendanceConcurrencyService;
import com.mycompany.myapp.utils.SecurityUtils;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class MockConcurrencyTestUI extends JPanel {
    private JButton btnMassTest;
    private JButton btnSpamTest;
    private JTextArea logArea;
    private final AttendanceConcurrencyService concurrencyService;
    private final int testScheduleId = 1; // ID ca học mẫu (Khớp với dữ liệu lớp số 1 dưới Oracle)

    public MockConcurrencyTestUI() {
        concurrencyService = new AttendanceConcurrencyService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh điều khiển chứa 2 kịch bản kiểm thử đồ án
        JPanel topPanel = new JPanel(new FlowLayout());
        btnMassTest = new JButton("1. Test 50 Học sinh quét cùng lúc");
        btnSpamTest = new JButton("2. Test 1 Học sinh spam 10 lần (Chống hack)");
        topPanel.add(btnMassTest);
        topPanel.add(btnSpamTest);
        add(topPanel, BorderLayout.NORTH);

        // Khung hiển thị nhật ký bắn request
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // ================= KỊCH BẢN 1: 50 HỌC SINH ĐỒNG LOẠT QUÉT MÃ =================
        btnMassTest.addActionListener(e -> {
            log(">>> KHỞI CHẠY GIẢ LẬP: 50 REQUEST QUÉT QR ĐỒNG THỜI...");
            String validToken = generateMockToken();
            
            // Thread Pool ngầm sẽ gắp lần lượt 50 request này xử lý song song
            for (int i = 1; i <= 50; i++) {
                concurrencyService.submitScanRequest(i, testScheduleId, validToken);
            }
        });

        // ================= KỊCH BẢN 2: CHỐNG SPAM GIAN LẬN (RACE CONDITION) =================
        btnSpamTest.addActionListener(e -> {
            log(">>> KHỞI CHẠY GIẢ LẬP: ATTACK RACE CONDITION (1 HỌC SINH SPAM 10 LẦN/MS)...");
            String validToken = generateMockToken();
            
            // Sử dụng CountDownLatch để giam 10 luồng lại, rồi "thả xích" chạy CÙNG 1 PHẦN NGHÌN GIÂY
            CountDownLatch latch = new CountDownLatch(1);

            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    try {
                        latch.await(); // Giam luồng chờ tín hiệu
                        concurrencyService.submitScanRequest(99, testScheduleId, validToken); // Học sinh ID 99 spam
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
            latch.countDown(); // Phát lệnh giải phóng! 10 luồng cùng lúc đâm thẳng vào Database Oracle
        });
    }

    /**
     * Hàm sinh mã QR giả lập hợp lệ để vượt qua tầng bảo mật mật mã (SHA-256) của Backend
     */
    private String generateMockToken() {
        String token = "TOKEN_LIVE_TEST";
        long exp = System.currentTimeMillis() + 60000; // Cấp hạn dùng 60 giây để thoải mái bấm test
        String payload = testScheduleId + "|" + token + "|" + exp;
        return payload + "|" + SecurityUtils.generateHash(payload);
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Tự động cuộn xuống dòng mới nhất
        });
    }

    /**
     * Hàm Main để bạn nhấp Shift + F6 chạy kiểm thử độc lập giao diện này trên NetBeans
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EduFlex - Hệ Thống Giả Lập Trạm Quét Mã Học Viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(650, 450);
            frame.setLocationRelativeTo(null); // Hiển thị căn giữa màn hình
            frame.add(new MockConcurrencyTestUI());
            frame.setVisible(true);
        });
    }
}