package com.mycompany.myapp.service;

import com.mycompany.myapp.repository.ConcurrencyAttendanceRepository;
import com.mycompany.myapp.utils.EventBus;
import com.mycompany.myapp.utils.SecurityUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AttendanceConcurrencyService {
    // Thread Pool quản trị luồng xử lý đồng thời, giới hạn tối đa 20 kết nối dội xuống DB cùng lúc
    private final ExecutorService executor = Executors.newFixedThreadPool(20);
    private final ConcurrencyAttendanceRepository repository = new ConcurrencyAttendanceRepository();

    /**
     * Tiếp nhận và phân phối request quét mã QR từ các trạm máy học viên
     */
    public void submitScanRequest(int studentId, int scheduleId, String scannedData) {
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();

            // 1. Kiểm tra cấu trúc phân đoạn của chuỗi Token QR
            String[] parts = scannedData.split("\\|");
            if (parts.length != 4) {
                System.out.println("[" + threadName + "] Từ chối: Định dạng chuỗi QR Code không hợp lệ!");
                return;
            }

            long expiredTime = Long.parseLong(parts[2]);
            String signature = parts[3];

            // 2. Kiểm tra thời gian hiệu lực (Lớp bảo mật chống gian lận bằng ảnh chụp cũ)
            if (System.currentTimeMillis() > expiredTime) {
                System.out.println("[" + threadName + "] Từ chối: Mã QR của Học viên " + studentId + " đã hết hạn sử dụng!");
                return;
            }

            // 3. Xác thực chữ ký bảo mật mật mã (Lớp bảo mật chống tự chế chuỗi ký tự)
            String payloadToCheck = parts[0] + "|" + parts[1] + "|" + parts[2];
            if (!SecurityUtils.generateHash(payloadToCheck).equals(signature)) {
                System.out.println("[" + threadName + "] Cảnh báo: Chữ ký số mã QR của Học viên " + studentId + " bị sai lệch hoàn toàn!");
                return;
            }

            // 4. Gọi xuống tầng Repository để truy vấn xử lý khóa giao dịch
            boolean success = repository.processAttendanceWithLock(scheduleId, studentId, parts[1]);
            long latency = System.currentTimeMillis() - startTime;

            if (success) {
                System.out.println("[" + threadName + "] Xử lý THÀNH CÔNG -> Học viên ID: " + studentId + " (" + latency + "ms)");
                // Bắn tín hiệu EventBus thời gian thực để ép giao diện Swing nảy số màu xanh lập tức
                EventBus.publish(studentId, "PRESENT");
            } else {
                // Đã đồng bộ: Nhường quyền in chi tiết mã lỗi ORA cho tầng Repository, tránh gây hiểu lầm log
                System.out.println("[" + threadName + "] Xử lý THẤT BẠI ở tầng DB -> Học viên ID: " + studentId);
            }
        });
    }

    /**
     * Giải phóng an toàn hệ thống Thread Pool khi tắt ứng dụng
     */
    public void shutdown() {
        try {
            executor.shutdown();
            // Chờ tối đa 3 giây để các luồng đang chạy nốt tiến trình điểm danh dở dang
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}