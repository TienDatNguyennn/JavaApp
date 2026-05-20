package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.AccountUpdateDTO;
import com.mycompany.myapp.service.AccountService;
import com.mycompany.myapp.exception.DuplicateDataException;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;
import com.mycompany.myapp.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class TeacherProfilePanel extends JPanel {

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtIdentityCard;
    private JTextField txtMajor;
    private JTextField txtDegree;

    private JLabel lblStatus;
    private JLabel lblNameSummary;
    private JLabel lblEmailSummary;
    private JLabel lblRoleSummary;

    private JButton btnSave;
    private JButton btnReload;

    private AccountService accountService;
    private int currentAccountId;
    private int currentUserId;

    private static final Color BG_PAGE = new Color(248, 250, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_SOFT = new Color(220, 252, 231);

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    private static final Color RED = new Color(220, 38, 38);
    private static final Color DISABLED_BG = new Color(248, 250, 252);
    private static final Color DISABLED_TEXT = new Color(100, 116, 139);

    public TeacherProfilePanel() {
        accountService = new AccountService();
        currentAccountId = SessionStore.getAccountId();

        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hồ sơ cá nhân");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Quản lý thông tin liên hệ và hồ sơ giảng viên đang đăng nhập");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        btnReload = createOutlineButton("Làm mới");
        btnReload.setPreferredSize(new Dimension(105, 38));
        btnReload.addActionListener(e -> loadProfileData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnReload);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        content.add(buildSummaryCard(), BorderLayout.WEST);
        content.add(buildFormCard(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildSummaryCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(0, 18));
        card.setPreferredSize(new Dimension(315, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(22, 22, 22, 22)
        ));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        ProfileAvatar avatar = new ProfileAvatar();
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblNameSummary = new JLabel("Giảng viên");
        lblNameSummary.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNameSummary.setForeground(TEXT_MAIN);
        lblNameSummary.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblEmailSummary = new JLabel("email@example.com");
        lblEmailSummary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEmailSummary.setForeground(TEXT_MUTED);
        lblEmailSummary.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRoleSummary = new JLabel("Tài khoản giảng viên");
        lblRoleSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRoleSummary.setForeground(PRIMARY_DARK);
        lblRoleSummary.setOpaque(true);
        lblRoleSummary.setBackground(PRIMARY_SOFT);
        lblRoleSummary.setBorder(new EmptyBorder(7, 12, 7, 12));
        lblRoleSummary.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(avatar);
        top.add(Box.createVerticalStrut(16));
        top.add(lblNameSummary);
        top.add(Box.createVerticalStrut(6));
        top.add(lblEmailSummary);
        top.add(Box.createVerticalStrut(14));
        top.add(lblRoleSummary);

        JPanel note = new JPanel(new BorderLayout(10, 0));
        note.setOpaque(true);
        note.setBackground(new Color(248, 250, 252));
        note.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel noteIcon = new InfoIconPanel(ORANGE, ORANGE_SOFT);
        JLabel noteText = new JLabel("<html><div style='width:195px;'>Chuyên môn và bằng cấp do quản trị viên cập nhật để đảm bảo dữ liệu nghiệp vụ.</div></html>");
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteText.setForeground(TEXT_MUTED);

        note.add(noteIcon, BorderLayout.WEST);
        note.add(noteText, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(note, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildFormCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JPanel formHeader = new JPanel(new BorderLayout(12, 0));
        formHeader.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thông tin hồ sơ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Các trường có dấu (*) là thông tin bắt buộc");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        formHeader.add(titleBox, BorderLayout.WEST);

        JPanel formBody = new JPanel(new GridBagLayout());
        formBody.setOpaque(false);

        txtFullName = createTextField(true);
        txtEmail = createTextField(true);
        txtPhone = createTextField(true);
        txtIdentityCard = createTextField(true);
        txtMajor = createTextField(false);
        txtDegree = createTextField(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 14, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addInput(formBody, gbc, 0, 0, "Họ và tên (*)", txtFullName);
        addInput(formBody, gbc, 1, 0, "Email (*)", txtEmail);

        addInput(formBody, gbc, 0, 1, "Số điện thoại (*)", txtPhone);
        addInput(formBody, gbc, 1, 1, "Căn cước công dân (*)", txtIdentityCard);

        addInput(formBody, gbc, 0, 2, "Chuyên môn", txtMajor);
        addInput(formBody, gbc, 1, 2, "Bằng cấp", txtDegree);

        JPanel adminHint = new JPanel(new BorderLayout(10, 0));
        adminHint.setOpaque(true);
        adminHint.setBackground(new Color(248, 250, 252));
        adminHint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel lockIcon = new LockIconPanel(PRIMARY, PRIMARY_SOFT);
        JLabel hint = new JLabel("Chuyên môn và bằng cấp đang ở chế độ chỉ xem. Liên hệ quản trị viên khi cần điều chỉnh.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);

        adminHint.add(lockIcon, BorderLayout.WEST);
        adminHint.add(hint, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        formBody.add(adminHint, gbc);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel saveNote = new JLabel("Kiểm tra đúng email, SĐT và CCCD trước khi cập nhật.");
        saveNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveNote.setForeground(TEXT_MUTED);

        btnSave = createFilledButton("Cập nhật hồ sơ", PRIMARY);
        btnSave.setPreferredSize(new Dimension(165, 42));
        btnSave.addActionListener((ActionEvent e) -> handleUpdateProfile());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnSave);

        footer.add(saveNote, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        card.add(formHeader, BorderLayout.NORTH);
        card.add(formBody, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void addInput(JPanel panel, GridBagConstraints gbc, int x, int y, String label, JTextField field) {
        JPanel input = createInput(label, field);

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 14, x == 0 ? 18 : 0);
        panel.add(input, gbc);
    }

    private JPanel createInput(String label, JTextField txtField) {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MAIN);

        p.add(lbl, BorderLayout.NORTH);
        p.add(txtField, BorderLayout.CENTER);

        return p;
    }

    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(260, 40));
        field.setBackground(editable ? Color.WHITE : DISABLED_BG);
        field.setForeground(editable ? TEXT_MAIN : DISABLED_TEXT);
        field.setBorder(normalBorder());

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.isEditable()) field.setBorder(focusBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.isEditable()) field.setBorder(normalBorder());
            }
        });

        return field;
    }

    private JButton createFilledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return button;
    }

    private Border normalBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)
        );
    }

    private Border focusBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)
        );
    }

    private Border errorBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RED, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    private void loadProfileData() {
        if (currentAccountId <= 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setLoadingState(true, "Đang tải hồ sơ...");

        SwingWorker<Result<AccountUpdateDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<AccountUpdateDTO> doInBackground() {
                return accountService.getAccountInfo(currentAccountId);
            }

            @Override
            protected void done() {
                try {
                    Result<AccountUpdateDTO> result = get();

                    if (result.isSuccess()) {
                        AccountUpdateDTO data = result.getData();
                        currentUserId = data.getUserId();

                        txtFullName.setText(safe(data.getFullName()));
                        txtEmail.setText(safe(data.getEmail()));
                        txtPhone.setText(safe(data.getPhone()));
                        txtIdentityCard.setText(data.getIdentityCard() != null ? data.getIdentityCard() : "");

                        lblNameSummary.setText(safe(data.getFullName()).isEmpty() ? "Giảng viên" : data.getFullName());
                        lblEmailSummary.setText(safe(data.getEmail()).isEmpty() ? "Chưa có email" : data.getEmail());

                        setLoadingState(false, "Dữ liệu đã cập nhật");
                    } else {
                        setLoadingState(false, "Lỗi tải dữ liệu");
                        JOptionPane.showMessageDialog(TeacherProfilePanel.this, result.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    setLoadingState(false, "Lỗi tải dữ liệu");
                    JOptionPane.showMessageDialog(
                            TeacherProfilePanel.this,
                            "Không thể tải hồ sơ: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void handleUpdateProfile() {
        resetBorders();

        AccountUpdateDTO dto = new AccountUpdateDTO();
        dto.setAccountId(currentAccountId);
        dto.setUserId(currentUserId);
        dto.setFullName(txtFullName.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setPhone(txtPhone.getText().trim());
        dto.setIdentityCard(txtIdentityCard.getText().trim());

        if (dto.getFullName().isEmpty() || dto.getEmail().isEmpty() || dto.getPhone().isEmpty() || dto.getIdentityCard().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường bắt buộc (*).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setLoadingState(true, "Đang cập nhật hồ sơ...");

        SwingWorker<Result<Void>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<Void> doInBackground() {
                return accountService.updateProfile(dto);
            }

            @Override
            protected void done() {
                try {
                    Result<Void> result = get();

                    if (result.isSuccess()) {
                        lblNameSummary.setText(dto.getFullName());
                        lblEmailSummary.setText(dto.getEmail());
                        JOptionPane.showMessageDialog(TeacherProfilePanel.this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TeacherProfilePanel.this, result.getMessage(), "Thất bại", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();

                    if (cause instanceof DuplicateDataException) {
                        DuplicateDataException ex = (DuplicateDataException) cause;
                        highlightErrorField(ex.getErrorField());
                        JOptionPane.showMessageDialog(TeacherProfilePanel.this, ex.getMessage(), "Dữ liệu trùng lặp", JOptionPane.WARNING_MESSAGE);
                    } else if (cause instanceof IllegalArgumentException) {
                        JOptionPane.showMessageDialog(TeacherProfilePanel.this, cause.getMessage(), "Cảnh báo nhập liệu", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(
                                TeacherProfilePanel.this,
                                "Không thể cập nhật hồ sơ: " + e.getMessage(),
                                "Lỗi dữ liệu",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } finally {
                    setLoadingState(false, "Sẵn sàng");
                }
            }
        };

        worker.execute();
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) lblStatus.setText(message);
        if (btnSave != null) btnSave.setEnabled(!loading);
        if (btnReload != null) btnReload.setEnabled(!loading);
    }

    private void highlightErrorField(String fieldName) {
        switch (fieldName) {
            case "email":
                txtEmail.setBorder(errorBorder());
                txtEmail.requestFocus();
                break;
            case "phone":
                txtPhone.setBorder(errorBorder());
                txtPhone.requestFocus();
                break;
            case "identityCard":
                txtIdentityCard.setBorder(errorBorder());
                txtIdentityCard.requestFocus();
                break;
            default:
                break;
        }
    }

    private void resetBorders() {
        txtFullName.setBorder(normalBorder());
        txtEmail.setBorder(normalBorder());
        txtPhone.setBorder(normalBorder());
        txtIdentityCard.setBorder(normalBorder());
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private static class ProfileAvatar extends JPanel {
        ProfileAvatar() {
            setOpaque(false);
            setPreferredSize(new Dimension(92, 92));
            setMinimumSize(new Dimension(92, 92));
            setMaximumSize(new Dimension(92, 92));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(PRIMARY_SOFT);
            g2.fillOval(0, 0, 92, 92);

            g2.setColor(PRIMARY);
            g2.fillOval(33, 21, 26, 26);
            g2.fillRoundRect(24, 53, 44, 24, 20, 20);

            g2.dispose();
        }
    }

    private static abstract class SmallIconPanel extends JPanel {
        private final Color accent;
        private final Color soft;

        SmallIconPanel(Color accent, Color soft) {
            this.accent = accent;
            this.soft = soft;
            setOpaque(false);
            setPreferredSize(new Dimension(34, 34));
            setMinimumSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(soft);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g2);

            g2.dispose();
        }

        protected abstract void drawIcon(Graphics2D g2);
    }

    private static class LockIconPanel extends SmallIconPanel {
        LockIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRoundRect(10, 15, 14, 11, 3, 3);
            g2.drawArc(12, 8, 10, 12, 0, 180);
        }
    }

    private static class InfoIconPanel extends SmallIconPanel {
        InfoIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(10, 10, 14, 14);
            g2.drawLine(17, 16, 17, 21);
            g2.fillOval(16, 13, 2, 2);
        }
    }
}
