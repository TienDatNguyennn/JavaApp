package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.SubjectDTO;
import com.mycompany.myapp.repository.SubjectDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.regex.Pattern;

public class ManageSubjectPanel extends JPanel {

    private JTable tblSubjects;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private SubjectDAO subjectDAO = new SubjectDAO();

    private JLabel lblTotal;
    private JLabel lblActive;
    private JLabel lblInactive;
    private JLabel lblShowing;
    private JLabel lblStatus;
    private JLabel lblFilterBadge;

    private JComboBox<String> cbxFilterStatus;
    private JTextField txtSearch;

    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;

    private static final Color BG_PAGE = new Color(248, 250, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_SOFT = new Color(219, 234, 254);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_SOFT = new Color(220, 252, 231);

    private static final Color RED = new Color(220, 38, 38);
    private static final Color RED_SOFT = new Color(254, 226, 226);

    private static final String SEARCH_PLACEHOLDER = "Tìm theo mã, tên môn học hoặc mô tả...";

    public ManageSubjectPanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContentCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Quản lý môn học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Theo dõi danh mục môn học, trạng thái đào tạo và thao tác thêm/sửa/ngừng đào tạo");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        btnRefresh = createOutlineButton("Làm mới", PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(105, 38));
        btnRefresh.addActionListener(e -> loadData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnRefresh);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        JPanel metricRow = new JPanel(new GridLayout(1, 4, 14, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 88));

        lblTotal = new JLabel("0");
        lblActive = new JLabel("0");
        lblInactive = new JLabel("0");
        lblShowing = new JLabel("0");

        metricRow.add(createMetricCard("Tổng môn học", lblTotal, "Môn", new BookIconPanel(PRIMARY, PRIMARY_SOFT), PRIMARY));
        metricRow.add(createMetricCard("Đang giảng dạy", lblActive, "Môn", new CheckIconPanel(GREEN, GREEN_SOFT), GREEN));
        metricRow.add(createMetricCard("Ngừng đào tạo", lblInactive, "Môn", new StopIconPanel(RED, RED_SOFT), RED));
        metricRow.add(createMetricCard("Đang hiển thị", lblShowing, "Môn", new ViewIconPanel(BLUE, BLUE_SOFT), BLUE));

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(metricRow, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildContentCard() {
        RoundedCardPanel card = new RoundedCardPanel(18, BG_CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 12));
        toolbar.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Danh sách môn học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Tìm kiếm, lọc trạng thái và quản lý danh mục môn học trong hệ thống");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        lblFilterBadge = new JLabel("Tất cả");
        lblFilterBadge.setOpaque(true);
        lblFilterBadge.setBackground(PRIMARY_SOFT);
        lblFilterBadge.setForeground(PRIMARY_DARK);
        lblFilterBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFilterBadge.setBorder(new EmptyBorder(7, 12, 7, 12));

        top.add(titleBox, BorderLayout.WEST);
        top.add(lblFilterBadge, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(true);
        filterPanel.setBackground(new Color(248, 250, 252));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblStatusFilter = toolbarLabel("Trạng thái");
        cbxFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Đang giảng dạy", "Ngừng đào tạo"});
        cbxFilterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxFilterStatus.setBackground(Color.WHITE);
        cbxFilterStatus.setPreferredSize(new Dimension(165, 36));
        cbxFilterStatus.addActionListener(e -> applyFilter());

        JLabel lblSearch = toolbarLabel("Tìm kiếm");
        txtSearch = new JTextField(SEARCH_PLACEHOLDER);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setForeground(TEXT_MUTED);
        txtSearch.setPreferredSize(new Dimension(320, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 11, 0, 11)
        ));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(txtSearch.getText())) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_MAIN);
                }
                txtSearch.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1, true),
                        new EmptyBorder(0, 11, 0, 11)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(SEARCH_PLACEHOLDER);
                    txtSearch.setForeground(TEXT_MUTED);
                }
                txtSearch.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(0, 11, 0, 11)
                ));
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        btnAdd = createFilledButton("+ Thêm mới", GREEN);
        btnEdit = createFilledButton("Sửa", BLUE);
        btnDelete = createOutlineButton("Ngừng đào tạo", RED);

        btnAdd.setPreferredSize(new Dimension(125, 36));
        btnEdit.setPreferredSize(new Dimension(88, 36));
        btnDelete.setPreferredSize(new Dimension(135, 36));

        setupActionListeners(btnAdd, btnEdit, btnDelete);

        gbc.gridx = 0;
        gbc.weightx = 0;
        filterPanel.add(lblStatusFilter, gbc);

        gbc.gridx = 1;
        filterPanel.add(cbxFilterStatus, gbc);

        gbc.gridx = 2;
        filterPanel.add(lblSearch, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        filterPanel.add(txtSearch, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        filterPanel.add(btnAdd, gbc);

        gbc.gridx = 5;
        filterPanel.add(btnEdit, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(btnDelete, gbc);

        toolbar.add(top, BorderLayout.NORTH);
        toolbar.add(filterPanel, BorderLayout.CENTER);

        return toolbar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(new String[]{"STT", "Mã môn", "Tên môn học", "Mô tả", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblSubjects = new JTable(tableModel);
        configureTable(tblSubjects);

        rowSorter = new TableRowSorter<>(tableModel);
        tblSubjects.setRowSorter(rowSorter);

        tblSubjects.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(tblSubjects);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new SubjectCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(420);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
    }

    private JLabel toolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MAIN);
        return label;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String unit, JPanel iconBox, Color accent) {
        RoundedCardPanel card = new RoundedCardPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_MUTED);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valueRow.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accent);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUnit.setForeground(accent);

        valueRow.add(valueLabel);
        valueRow.add(lblUnit);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(7));
        info.add(valueRow);

        card.add(iconBox, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private JButton createFilledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(bg);
            }
        });

        return btn;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(color);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(new Color(248, 250, 252));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private void setupActionListeners(JButton btnAdd, JButton btnEdit, JButton btnDelete) {
        btnAdd.addActionListener(e -> {
            SubjectDialog dialog = new SubjectDialog(SwingUtilities.getWindowAncestor(this), "Thêm Môn Học Mới", null);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                setLoadingState(true, "Đang thêm môn học...");
                if (subjectDAO.insertSubject(dialog.getSubjectData())) {
                    JOptionPane.showMessageDialog(this, "Đã thêm môn học thành công!");
                    loadData();
                } else {
                    setLoadingState(false, "Thêm thất bại");
                    JOptionPane.showMessageDialog(this, "Thêm môn học thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int viewRow = tblSubjects.getSelectedRow();

            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn môn học cần sửa.", "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int row = tblSubjects.convertRowIndexToModel(viewRow);

            SubjectDTO s = new SubjectDTO(
                    Integer.parseInt(tableModel.getValueAt(row, 1).toString()),
                    tableModel.getValueAt(row, 2).toString(),
                    tableModel.getValueAt(row, 3).toString(),
                    tableModel.getValueAt(row, 4).toString()
            );

            SubjectDialog dialog = new SubjectDialog(SwingUtilities.getWindowAncestor(this), "Sửa Môn Học", s);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                setLoadingState(true, "Đang cập nhật...");
                if (subjectDAO.updateSubject(dialog.getSubjectData())) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật môn học!");
                    loadData();
                } else {
                    setLoadingState(false, "Cập nhật thất bại");
                    JOptionPane.showMessageDialog(this, "Cập nhật môn học thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int viewRow = tblSubjects.getSelectedRow();

            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn môn học cần ngừng đào tạo.", "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int row = tblSubjects.convertRowIndexToModel(viewRow);
            String subjectName = tableModel.getValueAt(row, 2).toString();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn chắc chắn muốn chuyển môn \"" + subjectName + "\" sang trạng thái ngừng đào tạo?",
                    "Xác nhận ngừng đào tạo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                setLoadingState(true, "Đang cập nhật trạng thái...");
                int subjectId = Integer.parseInt(tableModel.getValueAt(row, 1).toString());

                if (subjectDAO.deleteSubject(subjectId)) {
                    loadData();
                } else {
                    setLoadingState(false, "Cập nhật thất bại");
                    JOptionPane.showMessageDialog(this, "Không thể ngừng đào tạo môn học này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadData() {
        setLoadingState(true, "Đang tải dữ liệu...");

        SwingWorker<List<SubjectDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SubjectDTO> doInBackground() {
                return subjectDAO.getAllSubjects();
            }

            @Override
            protected void done() {
                try {
                    List<SubjectDTO> list = get();
                    tableModel.setRowCount(0);

                    int countTotal = 0;
                    int countActive = 0;
                    int countInactive = 0;
                    int stt = 1;

                    for (SubjectDTO s : list) {
                        countTotal++;

                        if ("Đang giảng dạy".equals(s.getStatus())) {
                            countActive++;
                        } else {
                            countInactive++;
                        }

                        tableModel.addRow(new Object[]{
                                stt++,
                                s.getSubjectId(),
                                safe(s.getSubjectName()),
                                safe(s.getDescription()),
                                safe(s.getStatus())
                        });
                    }

                    lblTotal.setText(String.valueOf(countTotal));
                    lblActive.setText(String.valueOf(countActive));
                    lblInactive.setText(String.valueOf(countInactive));

                    applyFilter();
                    setLoadingState(false, "Dữ liệu đã cập nhật");

                } catch (Exception e) {
                    setLoadingState(false, "Lỗi tải dữ liệu");
                    JOptionPane.showMessageDialog(
                            ManageSubjectPanel.this,
                            "Không thể tải danh sách môn học: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void applyFilter() {
        if (rowSorter == null) return;

        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();
        if (SEARCH_PLACEHOLDER.equals(keyword)) keyword = "";

        String filterStatus = cbxFilterStatus == null || cbxFilterStatus.getSelectedItem() == null
                ? "Tất cả"
                : cbxFilterStatus.getSelectedItem().toString();

        RowFilter<DefaultTableModel, Object> keywordFilter = null;
        RowFilter<DefaultTableModel, Object> statusFilter = null;

        if (!keyword.isEmpty()) {
            keywordFilter = RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 1, 2, 3);
        }

        if (!"Tất cả".equals(filterStatus)) {
            statusFilter = RowFilter.regexFilter("^" + Pattern.quote(filterStatus) + "$", 4);
        }

        if (keywordFilter != null && statusFilter != null) {
            rowSorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(keywordFilter, statusFilter)));
        } else if (keywordFilter != null) {
            rowSorter.setRowFilter(keywordFilter);
        } else if (statusFilter != null) {
            rowSorter.setRowFilter(statusFilter);
        } else {
            rowSorter.setRowFilter(null);
        }

        updateShowingCounter(filterStatus);
    }

    private void updateShowingCounter(String filterStatus) {
        int showing = tblSubjects == null ? 0 : tblSubjects.getRowCount();
        lblShowing.setText(String.valueOf(showing));

        if (lblFilterBadge != null) {
            lblFilterBadge.setText(filterStatus + " · " + showing + " môn");
        }
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) lblStatus.setText(message);

        if (btnAdd != null) btnAdd.setEnabled(!loading);
        if (btnEdit != null) btnEdit.setEnabled(!loading);
        if (btnDelete != null) btnDelete.setEnabled(!loading);
        if (btnRefresh != null) btnRefresh.setEnabled(!loading);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private static class SubjectCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setToolTipText(value == null ? "" : value.toString());
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(column == 1 ? PRIMARY_DARK : TEXT_MAIN);
            }

            if (column == 0 || column == 1 || column == 4) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", column == 1 ? Font.BOLD : Font.PLAIN, 13));
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            return label;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String status = value == null ? "" : value.toString();
            JLabel label = new JLabel(status, SwingConstants.CENTER);

            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 8, 5, 8));

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
                return label;
            }

            if ("Đang giảng dạy".equals(status)) {
                label.setBackground(GREEN_SOFT);
                label.setForeground(GREEN);
            } else {
                label.setBackground(RED_SOFT);
                label.setForeground(RED);
            }

            return label;
        }
    }

    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedCardPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static abstract class MetricIconPanel extends JPanel {
        private final Color accent;
        private final Color soft;

        MetricIconPanel(Color accent, Color soft) {
            this.accent = accent;
            this.soft = soft;
            setOpaque(false);
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(soft);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g2);

            g2.dispose();
        }

        protected abstract void drawIcon(Graphics2D g2);
    }

    private static class BookIconPanel extends MetricIconPanel {
        BookIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRoundRect(13, 10, 18, 24, 4, 4);
            g2.drawLine(18, 15, 27, 15);
            g2.drawLine(18, 20, 27, 20);
            g2.drawLine(18, 25, 24, 25);
        }
    }

    private static class CheckIconPanel extends MetricIconPanel {
        CheckIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 20, 20);
            g2.drawLine(17, 23, 21, 27);
            g2.drawLine(21, 27, 30, 17);
        }
    }

    private static class StopIconPanel extends MetricIconPanel {
        StopIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 20, 20);
            g2.drawLine(17, 17, 27, 27);
            g2.drawLine(27, 17, 17, 27);
        }
    }

    private static class ViewIconPanel extends MetricIconPanel {
        ViewIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(11, 15, 22, 14);
            g2.fillOval(20, 20, 4, 4);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        JFrame f = new JFrame("SIS - Quản Lý Môn Học");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1180, 720);
        f.setLocationRelativeTo(null);
        f.add(new ManageSubjectPanel());
        f.setVisible(true);
    }
}
