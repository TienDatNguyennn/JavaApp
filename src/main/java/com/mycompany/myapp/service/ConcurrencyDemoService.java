package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.repository.ConcurrencyDemoRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Orchestrates 4 concurrency demo scenarios:
 *   1. Lost Update (problem)  /  Lost Update (fixed with FOR UPDATE)
 *   2. Unrepeatable Read
 *   3. Phantom Read
 *   4. Deadlock
 *
 * Mỗi kịch bản dùng getNewConnection() / getNewConnectionReadCommitted()
 * để mỗi "giao dịch" có connection độc lập, giống môi trường thực tế.
 */
public class ConcurrencyDemoService {

    private final ConcurrencyDemoRepository repo = new ConcurrencyDemoRepository();

    // ════════════════════════════════════════════════════════════════
    // 1a. LOST UPDATE – MINH HỌA VẤN ĐỀ (READ COMMITTED, không FOR UPDATE)
    // ════════════════════════════════════════════════════════════════
    public void demoLostUpdateProblem(int invoiceId, Consumer<String> log) {
        log.accept("══════════════════════════════════════════");
        log.accept("KỊCH BẢN 1a: LOST UPDATE – VẤN ĐỀ");
        log.accept("Isolation: READ COMMITTED  |  Không dùng FOR UPDATE");
        log.accept("T1 (+1,000,000đ) và T2 (+500,000đ) cùng đọc invoice #" + invoiceId);
        log.accept("Kỳ vọng đúng: amount_paid tăng 1,500,000đ");
        log.accept("Kết quả thực tế: chỉ tăng 500,000đ → T1 bị MẤT!");
        log.accept("──────────────────────────────────────────");

        CountDownLatch bothRead = new CountDownLatch(2);

        // Lưu giá trị ban đầu để đối chiếu
        final double[] initial = {0};
        try (Connection c = DBConnection.getNewConnectionReadCommitted()) {
            initial[0] = repo.readAmountPaid(c, invoiceId);
            c.rollback();
            log.accept("[INFO] amount_paid ban đầu = " + initial[0]);
        } catch (SQLException e) {
            log.accept("[LỖI] Không đọc được dữ liệu ban đầu: " + e.getMessage());
            return;
        }

        Thread t1 = new Thread(() -> {
            try (Connection conn = DBConnection.getNewConnectionReadCommitted()) {
                double val = repo.readAmountPaid(conn, invoiceId);
                log.accept("[T1] Đọc amount_paid = " + val);
                bothRead.countDown();
                bothRead.await();            // Chờ T2 cũng đọc xong → race condition

                Thread.sleep(300);           // T1 xử lý chậm hơn T2
                double newVal = val + 1_000_000;
                repo.updateAmountPaid(conn, invoiceId, newVal);
                conn.commit();
                log.accept("[T1] COMMIT → amount_paid = " + newVal);
            } catch (Exception e) {
                log.accept("[T1] LỖI: " + e.getMessage());
            }
        }, "T1-LostUpdate");

        Thread t2 = new Thread(() -> {
            try (Connection conn = DBConnection.getNewConnectionReadCommitted()) {
                double val = repo.readAmountPaid(conn, invoiceId);
                log.accept("[T2] Đọc amount_paid = " + val);
                bothRead.countDown();
                bothRead.await();

                // T2 commit trước T1
                double newVal = val + 500_000;
                repo.updateAmountPaid(conn, invoiceId, newVal);
                conn.commit();
                log.accept("[T2] COMMIT → amount_paid = " + newVal + "  ← GHI ĐÈ LÊN T1!");
            } catch (Exception e) {
                log.accept("[T2] LỖI: " + e.getMessage());
            }
        }, "T2-LostUpdate");

        try {
            t1.start(); t2.start();
            t1.join(8000); t2.join(8000);

            try (Connection check = DBConnection.getNewConnectionReadCommitted()) {
                double actual = repo.readAmountPaid(check, invoiceId);
                check.rollback();
                log.accept("──────────────────────────────────────────");
                log.accept("[KẾT QUẢ] amount_paid = " + actual);
                double expected = initial[0] + 1_500_000;
                if (Math.abs(actual - expected) > 1) {
                    log.accept("[=> LOST UPDATE XẢY RA] Kỳ vọng " + expected + " nhưng thực tế " + actual);
                } else {
                    log.accept("[=> Không xảy ra lost update lần này (timing phụ thuộc DB)]");
                }
            }
        } catch (Exception e) {
            log.accept("[LỖI DEMO] " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 1b. LOST UPDATE – ĐÃ SỬA (SERIALIZABLE + SELECT FOR UPDATE)
    // ════════════════════════════════════════════════════════════════
    public void demoLostUpdateFixed(int invoiceId, Consumer<String> log) {
        log.accept("══════════════════════════════════════════");
        log.accept("KỊCH BẢN 1b: LOST UPDATE – ĐÃ SỬA");
        log.accept("Isolation: SERIALIZABLE  |  Dùng SELECT FOR UPDATE");
        log.accept("T2 sẽ bị CHẶN đến khi T1 COMMIT → không mất dữ liệu");
        log.accept("──────────────────────────────────────────");

        final double[] initial = {0};
        try (Connection c = DBConnection.getNewConnection()) {
            initial[0] = repo.readAmountPaid(c, invoiceId);
            c.rollback();
            log.accept("[INFO] amount_paid ban đầu = " + initial[0]);
        } catch (SQLException e) {
            log.accept("[LỖI] " + e.getMessage()); return;
        }

        CountDownLatch t1Locked = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try (Connection conn = DBConnection.getNewConnection()) {
                double val = repo.readAmountPaidForUpdate(conn, invoiceId);
                log.accept("[T1] SELECT FOR UPDATE → Đã khóa hàng. amount_paid = " + val);
                t1Locked.countDown();

                Thread.sleep(2500); // Giả lập T1 đang tính toán
                double newVal = val + 1_000_000;
                repo.updateAmountPaid(conn, invoiceId, newVal);
                conn.commit();
                log.accept("[T1] COMMIT → amount_paid = " + newVal + ". Giải phóng khóa.");
            } catch (Exception e) {
                log.accept("[T1] LỖI: " + e.getMessage());
            }
        }, "T1-Fixed");

        Thread t2 = new Thread(() -> {
            try { t1Locked.await(); Thread.sleep(150); } catch (InterruptedException ignored) {}
            try (Connection conn = DBConnection.getNewConnection()) {
                log.accept("[T2] Thử SELECT FOR UPDATE invoice #" + invoiceId + " → ĐANG CHỜ T1...");
                double val = repo.readAmountPaidForUpdate(conn, invoiceId); // Bị block tại đây
                log.accept("[T2] Được giải phóng! Đọc amount_paid = " + val + " (đã bao gồm +1,000,000 của T1)");
                double newVal = val + 500_000;
                repo.updateAmountPaid(conn, invoiceId, newVal);
                conn.commit();
                log.accept("[T2] COMMIT → amount_paid = " + newVal);
            } catch (Exception e) {
                log.accept("[T2] LỖI: " + e.getMessage());
            }
        }, "T2-Fixed");

        try {
            t1.start(); t2.start();
            t1.join(10000); t2.join(10000);

            try (Connection check = DBConnection.getNewConnection()) {
                double actual = repo.readAmountPaid(check, invoiceId);
                check.rollback();
                double expected = initial[0] + 1_500_000;
                log.accept("──────────────────────────────────────────");
                log.accept("[KẾT QUẢ] amount_paid = " + actual);
                if (Math.abs(actual - expected) < 1) {
                    log.accept("[=> ĐÃ SỬA ✓] Cả T1 (+1,000,000) và T2 (+500,000) đều được ghi nhận.");
                } else {
                    log.accept("[=> Xem thêm log bên trên để phân tích.]");
                }
            }
        } catch (Exception e) {
            log.accept("[LỖI DEMO] " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 2. UNREPEATABLE READ
    // ════════════════════════════════════════════════════════════════
    public void demoUnrepeatableRead(int resultId, Consumer<String> log) {
        log.accept("══════════════════════════════════════════");
        log.accept("KỊCH BẢN 2: UNREPEATABLE READ");
        log.accept("T1 đọc COURSE_RESULT #" + resultId + " hai lần.");
        log.accept("T2 UPDATE điểm số ở giữa 2 lần đọc của T1.");
        log.accept("READ COMMITTED → T1 thấy 2 giá trị khác nhau (vấn đề).");
        log.accept("SERIALIZABLE  → T1 thấy snapshot nhất quán (đã sửa).");
        log.accept("──────────────────────────────────────────");

        CountDownLatch t1FirstRead = new CountDownLatch(1);
        CountDownLatch t2Done      = new CountDownLatch(1);

        // --- Vấn đề: READ COMMITTED ---
        log.accept("[READ COMMITTED]");
        runUnrepeatableReadScenario(resultId, log, true, t1FirstRead, t2Done);

        // Reset latches
        CountDownLatch t1FirstRead2 = new CountDownLatch(1);
        CountDownLatch t2Done2      = new CountDownLatch(1);

        // Khôi phục điểm về 7.0 trước khi chạy demo SERIALIZABLE
        try (Connection reset = DBConnection.getNewConnection()) {
            repo.updateFinalScore(reset, resultId, 7.0);
            reset.commit();
            log.accept("[RESET] Điểm khôi phục về 7.0");
        } catch (SQLException e) {
            log.accept("[LỖI RESET] " + e.getMessage());
        }

        // --- Đã sửa: SERIALIZABLE ---
        log.accept("[SERIALIZABLE]");
        runUnrepeatableReadScenario(resultId, log, false, t1FirstRead2, t2Done2);
    }

    private void runUnrepeatableReadScenario(int resultId, Consumer<String> log,
            boolean useReadCommitted, CountDownLatch firstRead, CountDownLatch t2Done) {

        Thread t1 = new Thread(() -> {
            try (Connection conn = useReadCommitted
                    ? DBConnection.getNewConnectionReadCommitted()
                    : DBConnection.getNewConnection()) {

                double score1 = repo.readFinalScore(conn, resultId);
                log.accept("[T1] Lần đọc 1: final_score = " + score1);
                firstRead.countDown();
                t2Done.await();

                double score2 = repo.readFinalScore(conn, resultId);
                log.accept("[T1] Lần đọc 2: final_score = " + score2);
                if (Double.compare(score1, score2) != 0) {
                    log.accept("[T1] ⚠  UNREPEATABLE READ! " + score1 + " → " + score2);
                } else {
                    log.accept("[T1] ✓  Hai lần đọc nhất quán: " + score1);
                }
                conn.rollback();
            } catch (Exception e) {
                log.accept("[T1] LỖI: " + e.getMessage());
            }
        }, "T1-Unrepeatable");

        Thread t2 = new Thread(() -> {
            try { firstRead.await(); Thread.sleep(100); } catch (InterruptedException ignored) {}
            try (Connection conn = DBConnection.getNewConnection()) {
                repo.updateFinalScore(conn, resultId, 9.5);
                conn.commit();
                log.accept("[T2] UPDATE final_score = 9.5 → COMMIT ✓");
                t2Done.countDown();
            } catch (Exception e) {
                log.accept("[T2] LỖI: " + e.getMessage());
                t2Done.countDown();
            }
        }, "T2-Modifier");

        try {
            t1.start(); t2.start();
            t1.join(8000); t2.join(8000);
        } catch (InterruptedException ignored) {}
    }

    // ════════════════════════════════════════════════════════════════
    // 3. PHANTOM READ
    // ════════════════════════════════════════════════════════════════
    public void demoPhantomRead(Consumer<String> log) {
        log.accept("══════════════════════════════════════════");
        log.accept("KỊCH BẢN 3: PHANTOM READ");
        log.accept("T1 đếm COUNT(STUDENT) hai lần.");
        log.accept("T2 INSERT một học sinh mới ở giữa 2 lần đếm.");
        log.accept("READ COMMITTED → T1 thấy 2 số đếm khác nhau (phantom).");
        log.accept("SERIALIZABLE  → T1 thấy snapshot nhất quán.");
        log.accept("──────────────────────────────────────────");

        String tempName = "PHANTOM_" + System.currentTimeMillis();

        // --- Vấn đề: READ COMMITTED ---
        log.accept("[READ COMMITTED]");
        runPhantomScenario(log, true, tempName + "_RC");

        // --- Đã sửa: SERIALIZABLE ---
        log.accept("[SERIALIZABLE]");
        runPhantomScenario(log, false, tempName + "_SER");

        // Dọn dẹp
        try (Connection cleanup = DBConnection.getNewConnection()) {
            repo.deleteTempStudents(cleanup, "PHANTOM_");
            cleanup.commit();
            log.accept("[Dọn dẹp] Đã xóa mềm các học sinh test PHANTOM_*");
        } catch (SQLException e) {
            log.accept("[LỖI Dọn dẹp] " + e.getMessage());
        }
    }

    private void runPhantomScenario(Consumer<String> log, boolean useReadCommitted, String tempName) {
        CountDownLatch firstCount = new CountDownLatch(1);
        CountDownLatch t2Done     = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try (Connection conn = useReadCommitted
                    ? DBConnection.getNewConnectionReadCommitted()
                    : DBConnection.getNewConnection()) {

                int count1 = repo.countActiveStudents(conn);
                log.accept("[T1] Lần đếm 1: COUNT = " + count1);
                firstCount.countDown();
                t2Done.await();

                int count2 = repo.countActiveStudents(conn);
                log.accept("[T1] Lần đếm 2: COUNT = " + count2);
                if (count1 != count2) {
                    log.accept("[T1] ⚠  PHANTOM READ! Lần 1=" + count1 + " Lần 2=" + count2);
                } else {
                    log.accept("[T1] ✓  Hai lần đếm nhất quán: " + count1);
                }
                conn.rollback();
            } catch (Exception e) {
                log.accept("[T1] LỖI: " + e.getMessage());
            }
        }, "T1-Phantom");

        Thread t2 = new Thread(() -> {
            try { firstCount.await(); Thread.sleep(100); } catch (InterruptedException ignored) {}
            try (Connection conn = DBConnection.getNewConnection()) {
                repo.insertTempStudent(conn, tempName);
                conn.commit();
                log.accept("[T2] INSERT học sinh '" + tempName + "' → COMMIT ✓");
                t2Done.countDown();
            } catch (Exception e) {
                log.accept("[T2] LỖI: " + e.getMessage());
                t2Done.countDown();
            }
        }, "T2-Inserter");

        try {
            t1.start(); t2.start();
            t1.join(8000); t2.join(8000);
        } catch (InterruptedException ignored) {}
    }

    // ════════════════════════════════════════════════════════════════
    // 4. DEADLOCK
    // ════════════════════════════════════════════════════════════════
    public void demoDeadlock(int invoiceId, int studentId, Consumer<String> log) {
        log.accept("══════════════════════════════════════════");
        log.accept("KỊCH BẢN 4: DEADLOCK");
        log.accept("T1: khóa INVOICE #" + invoiceId + " → rồi cố khóa STUDENT #" + studentId);
        log.accept("T2: khóa STUDENT #" + studentId + " → rồi cố khóa INVOICE #" + invoiceId);
        log.accept("=> Vòng chờ tròn → Oracle phát hiện và rollback 1 giao dịch (ORA-00060)");
        log.accept("──────────────────────────────────────────");

        CountDownLatch t1LockedInvoice  = new CountDownLatch(1);
        CountDownLatch t2LockedStudent  = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try (Connection conn = DBConnection.getNewConnection()) {
                repo.lockInvoiceRow(conn, invoiceId);
                log.accept("[T1] Đã khóa INVOICE #" + invoiceId);
                t1LockedInvoice.countDown();

                t2LockedStudent.await();     // Chờ T2 giữ khóa STUDENT
                Thread.sleep(200);

                log.accept("[T1] Cố khóa STUDENT #" + studentId + " → chờ T2 nhả...");
                repo.lockStudentRow(conn, studentId); // Bị block / deadlock tại đây
                conn.commit();
                log.accept("[T1] COMMIT ✓");
            } catch (SQLException e) {
                log.accept("[T1] ORACLE HỦY GIAO DỊCH → " + e.getMessage());
            } catch (InterruptedException ignored) {}
        }, "T1-Deadlock");

        Thread t2 = new Thread(() -> {
            try { t1LockedInvoice.await(); Thread.sleep(100); } catch (InterruptedException ignored) {}
            try (Connection conn = DBConnection.getNewConnection()) {
                repo.lockStudentRow(conn, studentId);
                log.accept("[T2] Đã khóa STUDENT #" + studentId);
                t2LockedStudent.countDown();

                Thread.sleep(200);
                log.accept("[T2] Cố khóa INVOICE #" + invoiceId + " → chờ T1 nhả...");
                repo.lockInvoiceRow(conn, invoiceId); // Deadlock
                conn.commit();
                log.accept("[T2] COMMIT ✓");
            } catch (SQLException e) {
                log.accept("[T2] ORACLE HỦY GIAO DỊCH → " + e.getMessage());
            } catch (InterruptedException ignored) {}
        }, "T2-Deadlock");

        try {
            t1.start(); t2.start();
            t1.join(12000); t2.join(12000);
            log.accept("──────────────────────────────────────────");
            log.accept("[KẾT QUẢ] Oracle đã phát hiện deadlock và tự động");
            log.accept("          rollback 1 giao dịch. Giao dịch kia hoàn tất bình thường.");
        } catch (InterruptedException e) {
            log.accept("[DEMO] Bị gián đoạn: " + e.getMessage());
        }
    }
}
