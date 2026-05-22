package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.service.ConcurrencyDemoService;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Giao diện demo 4 vấn đề truy xuất đồng thời.
 * Chạy độc lập: Shift+F6 trên file này trong NetBeans.
 *
 * Yêu cầu dữ liệu mẫu trong DB:
 *   - INVOICE    có invoice_id  = TEST_INVOICE_ID
 *   - COURSE_RESULT có result_id = TEST_RESULT_ID
 *   - STUDENT    có student_id  = TEST_STUDENT_ID
 * Điều chỉnh 3 hằng số bên dưới nếu cần.
 */
public class TransactionDemoUI extends JPanel {

    // ── Điều chỉnh để khớp với dữ liệu Oracle của bạn ──
    private static final int TEST_INVOICE_ID = 1;
    private static final int TEST_RESULT_ID  = 1;
    private static final int TEST_STUDENT_ID = 1;

    private final ConcurrencyDemoService demoService = new ConcurrencyDemoService();
    private JTextArea logArea;

    private JButton btnLostProblem;
    private JButton btnLostFixed;
    private JButton btnUnrepeatable;
    private JButton btnPhantom;
    private JButton btnDeadlock;
    private JButton btnClear;

    public TransactionDemoUI() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Tiêu đề ──
        JLabel title = new JLabel(
            "Demo Truy Xuất Đồng Thời – Oracle SERIALIZABLE Isolation",
            SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // ── Panel nút ──
        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        TitledBorder border = BorderFactory.createTitledBorder("Chọn kịch bản kiểm thử");
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        btnPanel.setBorder(border);

        btnLostProblem  = makeBtn("1a. Lost Update (Vấn đề)",    new Color(255, 160, 160));
        btnLostFixed    = makeBtn("1b. Lost Update (FOR UPDATE)", new Color(160, 220, 160));
        btnUnrepeatable = makeBtn("2.  Unrepeatable Read",        new Color(255, 210, 130));
        btnPhantom      = makeBtn("3.  Phantom Read",             new Color(160, 200, 255));
        btnDeadlock     = makeBtn("4.  Deadlock",                 new Color(210, 170, 255));
        btnClear        = makeBtn("Xóa Log",                      new Color(210, 210, 210));

        btnPanel.add(btnLostProblem);
        btnPanel.add(btnLostFixed);
        btnPanel.add(btnUnrepeatable);
        btnPanel.add(btnPhantom);
        btnPanel.add(btnDeadlock);
        btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.CENTER);

        // ── Log area ──
        logArea = new JTextArea(20, 70);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(18, 18, 28));
        logArea.setForeground(new Color(180, 255, 180));
        logArea.setCaretColor(Color.WHITE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Nhật ký thực thi"));
        add(scroll, BorderLayout.SOUTH);

        // ── Gắn sự kiện ──
        btnLostProblem.addActionListener(e ->
            run(() -> demoService.demoLostUpdateProblem(TEST_INVOICE_ID, this::log)));

        btnLostFixed.addActionListener(e ->
            run(() -> demoService.demoLostUpdateFixed(TEST_INVOICE_ID, this::log)));

        btnUnrepeatable.addActionListener(e ->
            run(() -> demoService.demoUnrepeatableRead(TEST_RESULT_ID, this::log)));

        btnPhantom.addActionListener(e ->
            run(() -> demoService.demoPhantomRead(this::log)));

        btnDeadlock.addActionListener(e ->
            run(() -> demoService.demoDeadlock(TEST_INVOICE_ID, TEST_STUDENT_ID, this::log)));

        btnClear.addActionListener(e -> logArea.setText(""));
    }

    // ── Chạy demo trên background thread, disable nút khi đang chạy ──
    private void run(Runnable task) {
        setAllEnabled(false);
        new Thread(() -> {
            try {
                task.run();
            } finally {
                SwingUtilities.invokeLater(() -> setAllEnabled(true));
            }
        }).start();
    }

    private void setAllEnabled(boolean enabled) {
        btnLostProblem.setEnabled(enabled);
        btnLostFixed.setEnabled(enabled);
        btnUnrepeatable.setEnabled(enabled);
        btnPhantom.setEnabled(enabled);
        btnDeadlock.setEnabled(enabled);
    }

    private void log(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Chạy độc lập để test ──
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EduFlex – Demo Concurrency (SERIALIZABLE)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(860, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new TransactionDemoUI());
            frame.setVisible(true);
        });
    }
}
