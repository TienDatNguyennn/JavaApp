    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.utils.QRUtils;
import com.mycompany.myapp.utils.SecurityUtils;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.UUID;

public class DynamicQRService {
    private Timer refreshTimer;
    private JLabel qrLabel;
    private int currentScheduleId;

    public DynamicQRService(JLabel qrLabel, int scheduleId) {
        this.qrLabel = qrLabel;
        this.currentScheduleId = scheduleId;
    }

    public void startSession() {
        // Cứ 15000ms (15 giây) sinh 1 mã mới
        refreshTimer = new Timer(15000, e -> generateTokenAndRender());
        generateTokenAndRender(); // Chạy ngay lần đầu
        refreshTimer.start();
    }

    public void stopSession() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
        qrLabel.setIcon(null);
        qrLabel.setText("Phiên điểm danh đã kết thúc");
    }

    private void generateTokenAndRender() {
        try {
            // 1. Sinh Token ngẫu nhiên (UUID) và Hạn sử dụng
            String randomToken = UUID.randomUUID().toString().substring(0, 8);
            long expiredTimeMs = System.currentTimeMillis() + 15000;

            // 2. Đóng gói Payload: scheduleId | token | expiredTime
            String payload = currentScheduleId + "|" + randomToken + "|" + expiredTimeMs;
            
            // 3. Ký bảo mật (Hash)
            String signature = SecurityUtils.generateHash(payload);
            String finalQRData = payload + "|" + signature;

            System.out.println("[QR Service] Đã tạo QR mới: Hết hạn sau 15s");

            // 4. Sinh ảnh và vẽ lên Swing
           // Sửa dòng 58 thành:
            BufferedImage image = QRUtils.generateQRImage(finalQRData, 300, 300);
            SwingUtilities.invokeLater(() -> {
                qrLabel.setText("");
                qrLabel.setIcon(new ImageIcon(image));
            });

            // TODO: (Mở rộng) Lưu 'randomToken' vào bảng ATTENDANCE_SESSION trong Oracle nếu cần track chi tiết

        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> qrLabel.setText("Lỗi sinh QR Code"));
        }
    }
}