package com.mycompany.myapp.service;

import com.mycompany.myapp.model.AttendanceScanResult;
import com.mycompany.myapp.repository.ConcurrencyAttendanceRepository;
import com.mycompany.myapp.utils.EventBus;
import com.mycompany.myapp.utils.SecurityUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceConcurrencyService {

    private final ExecutorService executor;
    private final ConcurrencyAttendanceRepository repository;

    public AttendanceConcurrencyService() {
        this.executor = Executors.newFixedThreadPool(20);
        this.repository = new ConcurrencyAttendanceRepository();
    }

    public void submitScanRequest(int studentId, int expectedScheduleId, String qrData) {
        executor.submit(() -> {
            AttendanceScanResult validation = validateQr(expectedScheduleId, studentId, qrData);

            if (!validation.isSuccess()) {
                logResult(validation);
                EventBus.publish(studentId, validation.getStatus().name());
                return;
            }

            AttendanceScanResult result = repository.processAttendanceWithLock(
                    expectedScheduleId,
                    studentId,
                    qrData
            );

            logResult(result);
            EventBus.publish(studentId, result.getStatus().name());
        });
    }

    private AttendanceScanResult validateQr(int expectedScheduleId, int studentId, String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.INVALID_QR,
                    studentId,
                    expectedScheduleId,
                    "QR rỗng."
            );
        }

        String[] parts = qrData.split("\\|");

        if (parts.length != 4) {
            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.INVALID_QR,
                    studentId,
                    expectedScheduleId,
                    "QR sai định dạng."
            );
        }

        try {
            int qrScheduleId = Integer.parseInt(parts[0]);
            String token = parts[1];
            long expiredTimeMs = Long.parseLong(parts[2]);
            String signature = parts[3];

            String payload = qrScheduleId + "|" + token + "|" + expiredTimeMs;
            String expectedSignature = SecurityUtils.generateHash(payload);

            if (!expectedSignature.equals(signature)) {
                return AttendanceScanResult.fail(
                        AttendanceScanResult.Status.INVALID_QR,
                        studentId,
                        expectedScheduleId,
                        "QR bị sửa đổi hoặc chữ ký không hợp lệ."
                );
            }

            if (System.currentTimeMillis() > expiredTimeMs) {
                return AttendanceScanResult.fail(
                        AttendanceScanResult.Status.EXPIRED_QR,
                        studentId,
                        expectedScheduleId,
                        "QR đã hết hạn."
                );
            }

            if (qrScheduleId != expectedScheduleId) {
                return AttendanceScanResult.fail(
                        AttendanceScanResult.Status.WRONG_SCHEDULE,
                        studentId,
                        expectedScheduleId,
                        "QR không thuộc lịch học hiện tại."
                );
            }

            return AttendanceScanResult.success(studentId, expectedScheduleId);

        } catch (Exception e) {
            return AttendanceScanResult.fail(
                    AttendanceScanResult.Status.INVALID_QR,
                    studentId,
                    expectedScheduleId,
                    "Không đọc được QR: " + e.getMessage()
            );
        }
    }

    private void logResult(AttendanceScanResult result) {
        String threadName = Thread.currentThread().getName();

        switch (result.getStatus()) {
            case SUCCESS:
                System.out.println(
                        "[" + threadName + "] THÀNH CÔNG -> Học viên ID: "
                                + result.getStudentId()
                                + " | scheduleId=" + result.getScheduleId()
                );
                break;

            case DUPLICATE:
                System.out.println(
                        "[" + threadName + "] BỊ CHẶN TRÙNG -> Học viên ID: "
                                + result.getStudentId()
                                + " | " + result.getMessage()
                );
                break;

            case STUDENT_NOT_IN_CLASS:
                System.out.println(
                        "[" + threadName + "] TỪ CHỐI NGHIỆP VỤ -> Học viên ID: "
                                + result.getStudentId()
                                + " | " + result.getMessage()
                );
                break;

            default:
                System.out.println(
                        "[" + threadName + "] THẤT BẠI -> Học viên ID: "
                                + result.getStudentId()
                                + " | status=" + result.getStatus()
                                + " | " + result.getMessage()
                );
                break;
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}