package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.StudyReportDTO;
import com.mycompany.myapp.repository.StudyReportDAO;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StudyReportPanel extends JPanel {

    private JTable tblReports;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private final StudyReportDAO reportDAO = new StudyReportDAO();
    private List<StudyReportDTO> allData = new ArrayList<>();

    private JLabel lblTotalStudents;
    private JLabel lblPassed;
    private JLabel lblFailed;
    private JLabel lblShowing;
    private JLabel lblStatus;
    private JLabel lblFilterBadge;
    private JLabel lblInsight;

    private JComboBox<String> cbxClassFilter;
    private JComboBox<String> cbxResultFilter;
    private JTextField txtSearchStudent;

    private JButton btnRefresh;
    private JButton btnExport;

    private boolean isDataLoaded = false;

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

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    private static final String ALL_CLASSES = "Tất cả lớp";
    private static final String ALL_RESULTS = "Tất cả kết quả";
    private static final String SEARCH_PLACEHOLDER = "Tìm theo mã hoặc họ tên học viên...";

    public StudyReportPanel() {
        initComponents();
        loadDataFromDB();
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

        JLabel title = new JLabel("Báo cáo học tập");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Theo dõi kết quả học tập, phân loại học viên và lọc nhanh theo lớp/kết quả");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        btnRefresh = createOutlineButton("Làm mới", PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(105, 38));
        btnRefresh.addActionListener(e -> loadDataFromDB());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnRefresh);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        JPanel metricRow = new JPanel(new GridLayout(1, 4, 14, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 88));

        lblTotalStudents = new JLabel("0");
        lblPassed = new JLabel("0");
        lblFailed = new JLabel("0");
        lblShowing = new JLabel("0");

        metricRow.add(createMetricCard("Tổng số kết quả", lblTotalStudents, "HV", new StudentIconPanel(PRIMARY, PRIMARY_SOFT), PRIMARY));
        metricRow.add(createMetricCard("Học viên đạt", lblPassed, "HV", new CheckIconPanel(GREEN, GREEN_SOFT), GREEN));
        metricRow.add(createMetricCard("Chưa đạt", lblFailed, "HV", new FailIconPanel(RED, RED_SOFT), RED));
        metricRow.add(createMetricCard("Đang hiển thị", lblShowing, "HV", new ViewIconPanel(BLUE, BLUE_SOFT), BLUE));

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

        JLabel title = new JLabel("Danh sách kết quả học tập");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        lblInsight = new JLabel("Chọn lớp/kết quả hoặc tìm nhanh theo mã, họ tên học viên");
        lblInsight.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInsight.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblInsight);

        lblFilterBadge = new JLabel("Tất cả · 0 HV");
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

        cbxClassFilter = new JComboBox<>();
        cbxClassFilter.setPreferredSize(new Dimension(190, 36));
        cbxClassFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxClassFilter.setBackground(Color.WHITE);
        cbxClassFilter.addActionListener(e -> applyFilters());

        cbxResultFilter = new JComboBox<>(new String[]{ALL_RESULTS, "ĐẠT", "CHƯA ĐẠT"});
        cbxResultFilter.setPreferredSize(new Dimension(150, 36));
        cbxResultFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxResultFilter.setBackground(Color.WHITE);
        cbxResultFilter.addActionListener(e -> applyFilters());

        txtSearchStudent = new JTextField(SEARCH_PLACEHOLDER);
        txtSearchStudent.setPreferredSize(new Dimension(310, 36));
        txtSearchStudent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchStudent.setForeground(TEXT_MUTED);
        txtSearchStudent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 11, 0, 11)
        ));

        txtSearchStudent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(txtSearchStudent.getText())) {
                    txtSearchStudent.setText("");
                    txtSearchStudent.setForeground(TEXT_MAIN);
                }
                txtSearchStudent.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1, true),
                        new EmptyBorder(0, 11, 0, 11)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearchStudent.getText().trim().isEmpty()) {
                    txtSearchStudent.setText(SEARCH_PLACEHOLDER);
                    txtSearchStudent.setForeground(TEXT_MUTED);
                }
                txtSearchStudent.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        new EmptyBorder(0, 11, 0, 11)
                ));
            }
        });

        txtSearchStudent.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        btnExport = createFilledButton("Xuất Excel", GREEN);
        btnExport.setPreferredSize(new Dimension(115, 36));
        btnExport.addActionListener(e -> exportCurrentViewToExcel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0;
        filterPanel.add(toolbarLabel("Lớp"), gbc);

        gbc.gridx = 1;
        filterPanel.add(cbxClassFilter, gbc);

        gbc.gridx = 2;
        filterPanel.add(toolbarLabel("Kết quả"), gbc);

        gbc.gridx = 3;
        filterPanel.add(cbxResultFilter, gbc);

        gbc.gridx = 4;
        filterPanel.add(toolbarLabel("Tìm kiếm"), gbc);

        gbc.gridx = 5;
        gbc.weightx = 1;
        filterPanel.add(txtSearchStudent, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(btnExport, gbc);

        toolbar.add(top, BorderLayout.NORTH);
        toolbar.add(filterPanel, BorderLayout.CENTER);

        return toolbar;
    }

    private JScrollPane buildTable() {
        String[] columns = {"STT", "Mã HV", "Họ và tên", "Lớp học", "Điểm TB", "Xếp loại", "Kết quả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblReports = new JTable(tableModel);
        configureTable(tblReports);

        rowSorter = new TableRowSorter<>(tableModel);
        tblReports.setRowSorter(rowSorter);

        tblReports.getColumnModel().getColumn(4).setCellRenderer(new ScoreRenderer());
        tblReports.getColumnModel().getColumn(5).setCellRenderer(new ClassificationRenderer());
        tblReports.getColumnModel().getColumn(6).setCellRenderer(new ResultBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(tblReports);
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

        table.setDefaultRenderer(Object.class, new ReportCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
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
        return btn;
    }

    private void loadDataFromDB() {
        setLoadingState(true, "Đang tải dữ liệu...");

        SwingWorker<List<StudyReportDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudyReportDTO> doInBackground() {
                return reportDAO.getAllReports();
            }

            @Override
            protected void done() {
                try {
                    allData = get();
                    if (allData == null) {
                        allData = new ArrayList<>();
                    }

                    Set<String> classNames = new HashSet<>();
                    int totalDB = 0;
                    int passedDB = 0;
                    int failedDB = 0;

                    for (StudyReportDTO r : allData) {
                        if (r.getClassName() != null && !r.getClassName().trim().isEmpty()) {
                            classNames.add(r.getClassName());
                        }

                        totalDB++;
                        if ("ĐẠT".equals(r.getResult())) {
                            passedDB++;
                        } else {
                            failedDB++;
                        }
                    }

                    lblTotalStudents.setText(String.valueOf(totalDB));
                    lblPassed.setText(String.valueOf(passedDB));
                    lblFailed.setText(String.valueOf(failedDB));

                    refreshClassFilter(classNames);

                    isDataLoaded = true;
                    renderAllRows();
                    applyFilters();

                    if (allData.isEmpty()) {
                        setLoadingState(false, "Không có dữ liệu");
                        if (lblInsight != null) {
                            lblInsight.setText("Chưa có dữ liệu báo cáo học tập trong hệ thống.");
                        }
                    } else {
                        setLoadingState(false, "Dữ liệu đã cập nhật");
                    }

                } catch (Exception e) {
                    setLoadingState(false, "Lỗi tải dữ liệu");
                    JOptionPane.showMessageDialog(
                            StudyReportPanel.this,
                            "Không thể tải báo cáo học tập: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void refreshClassFilter(Set<String> classNames) {
        Object selected = cbxClassFilter.getSelectedItem();

        cbxClassFilter.removeAllItems();
        cbxClassFilter.addItem(ALL_CLASSES);

        for (String cName : classNames) {
            cbxClassFilter.addItem(cName);
        }

        if (selected != null) {
            cbxClassFilter.setSelectedItem(selected.toString());
        }

        if (cbxClassFilter.getSelectedIndex() == -1) {
            cbxClassFilter.setSelectedIndex(0);
        }
    }

    private void renderAllRows() {
        tableModel.setRowCount(0);

        int stt = 1;

        for (StudyReportDTO r : allData) {
            tableModel.addRow(new Object[]{
                    stt++,
                    safe(r.getStudentId()),
                    safe(r.getFullName()),
                    safe(r.getClassName()),
                    r.getAverageScore(),
                    safe(r.getClassification()),
                    safe(r.getResult())
            });
        }
    }

    private void applyFilters() {
        if (!isDataLoaded || rowSorter == null) return;

        String rawKeyword = txtSearchStudent == null ? "" : txtSearchStudent.getText().trim();
        String keyword = SEARCH_PLACEHOLDER.equalsIgnoreCase(rawKeyword) ? "" : rawKeyword.toLowerCase();

        String classFilter = cbxClassFilter == null || cbxClassFilter.getSelectedItem() == null
                ? ALL_CLASSES
                : cbxClassFilter.getSelectedItem().toString();

        String resultFilter = cbxResultFilter == null || cbxResultFilter.getSelectedItem() == null
                ? ALL_RESULTS
                : cbxResultFilter.getSelectedItem().toString();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 1, 2));
        }

        if (!ALL_CLASSES.equals(classFilter)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(classFilter) + "$", 3));
        }

        if (!ALL_RESULTS.equals(resultFilter)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(resultFilter) + "$", 6));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        updateShowingInfo(classFilter, resultFilter);
    }

    private void updateShowingInfo(String classFilter, String resultFilter) {
        int showing = tblReports == null ? 0 : tblReports.getRowCount();
        lblShowing.setText(String.valueOf(showing));

        String badgeText = classFilter;
        if (ALL_CLASSES.equals(classFilter)) {
            badgeText = "Tất cả lớp";
        }

        if (!ALL_RESULTS.equals(resultFilter)) {
            badgeText += " · " + resultFilter;
        }

        lblFilterBadge.setText(badgeText + " · " + showing + " HV");

        if (showing == 0) {
            lblInsight.setText("Không có học viên phù hợp với điều kiện lọc hiện tại");
        } else {
            lblInsight.setText("Đang hiển thị " + showing + " học viên phù hợp với bộ lọc");
        }
    }

    private void exportCurrentViewToExcel() {
        if (tblReports == null || tblReports.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không có dữ liệu để xuất. Vui lòng tải hoặc điều chỉnh bộ lọc trước.",
                    "Không có dữ liệu",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất báo cáo học tập ra Excel");

        String defaultName = "BaoCaoHocTap_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xls";
        chooser.setSelectedFile(new File(defaultName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".xls")) {
            file = new File(file.getParentFile(), file.getName() + ".xls");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "File đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        setLoadingState(true, "Đang xuất Excel...");

        File targetFile = file;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                writeExcelXml(targetFile);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            StudyReportPanel.this,
                            "Đã xuất file Excel thành công:\n" + targetFile.getAbsolutePath(),
                            "Xuất Excel thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            StudyReportPanel.this,
                            "Không thể xuất Excel: " + e.getMessage(),
                            "Lỗi xuất Excel",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoadingState(false, "Dữ liệu đã cập nhật");
                }
            }
        };

        worker.execute();
    }

    private void writeExcelXml(File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
        )) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<?mso-application progid=\"Excel.Sheet\"?>\n");
            writer.write("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
            writer.write(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n");
            writer.write(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n");
            writer.write(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
            writer.write(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n");

            writer.write("<Styles>\n");
            writer.write("<Style ss:ID=\"Title\"><Font ss:Bold=\"1\" ss:Size=\"16\"/><Alignment ss:Horizontal=\"Center\"/></Style>\n");
            writer.write("<Style ss:ID=\"Meta\"><Font ss:Size=\"10\" ss:Color=\"#64748B\"/></Style>\n");
            writer.write("<Style ss:ID=\"Header\"><Font ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/><Interior ss:Color=\"#6C5CE7\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/><Borders>"
                    + borderXml() + "</Borders></Style>\n");
            writer.write("<Style ss:ID=\"Cell\"><Borders>" + borderXml() + "</Borders><Alignment ss:Vertical=\"Center\"/></Style>\n");
            writer.write("<Style ss:ID=\"Center\"><Borders>" + borderXml() + "</Borders><Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>\n");
            writer.write("<Style ss:ID=\"Score\"><Borders>" + borderXml() + "</Borders><Interior ss:Color=\"#FFFBEB\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>\n");
            writer.write("<Style ss:ID=\"Pass\"><Borders>" + borderXml() + "</Borders><Font ss:Bold=\"1\" ss:Color=\"#16A34A\"/><Interior ss:Color=\"#DCFCE7\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/></Style>\n");
            writer.write("<Style ss:ID=\"Fail\"><Borders>" + borderXml() + "</Borders><Font ss:Bold=\"1\" ss:Color=\"#DC2626\"/><Interior ss:Color=\"#FEE2E2\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/></Style>\n");
            writer.write("</Styles>\n");

            writer.write("<Worksheet ss:Name=\"Bao cao hoc tap\">\n");
            writer.write("<Table>\n");

            int[] widths = {55, 85, 220, 170, 90, 120, 100};
            for (int width : widths) {
                writer.write("<Column ss:Width=\"" + width + "\"/>\n");
            }

            writer.write("<Row ss:Height=\"28\"><Cell ss:MergeAcross=\"6\" ss:StyleID=\"Title\"><Data ss:Type=\"String\">BÁO CÁO HỌC TẬP</Data></Cell></Row>\n");
            writer.write("<Row><Cell ss:MergeAcross=\"6\" ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Ngày xuất: "
                    + xmlEscape(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))) + "</Data></Cell></Row>\n");
            writer.write("<Row><Cell ss:MergeAcross=\"6\" ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Bộ lọc: "
                    + xmlEscape(getFilterSummary()) + "</Data></Cell></Row>\n");
            writer.write("<Row><Cell ss:MergeAcross=\"6\" ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Tổng dòng xuất: "
                    + tblReports.getRowCount() + "</Data></Cell></Row>\n");
            writer.write("<Row></Row>\n");

            writer.write("<Row>\n");
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">"
                        + xmlEscape(tableModel.getColumnName(col)) + "</Data></Cell>\n");
            }
            writer.write("</Row>\n");

            for (int viewRow = 0; viewRow < tblReports.getRowCount(); viewRow++) {
                int modelRow = tblReports.convertRowIndexToModel(viewRow);
                writer.write("<Row>\n");

                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(modelRow, col);
                    String style = getExcelStyleForCell(col, value);
                    String type = isNumericColumn(col, value) ? "Number" : "String";

                    writer.write("<Cell ss:StyleID=\"" + style + "\"><Data ss:Type=\"" + type + "\">"
                            + xmlEscape(formatExcelValue(value, col)) + "</Data></Cell>\n");
                }

                writer.write("</Row>\n");
            }

            writer.write("</Table>\n");
            writer.write("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\"><FreezePanes/><FrozenNoSplit/><SplitHorizontal>6</SplitHorizontal><TopRowBottomPane>6</TopRowBottomPane></WorksheetOptions>\n");
            writer.write("</Worksheet>\n");
            writer.write("</Workbook>\n");
        }
    }

    private String getFilterSummary() {
        String classFilter = cbxClassFilter == null || cbxClassFilter.getSelectedItem() == null
                ? ALL_CLASSES
                : cbxClassFilter.getSelectedItem().toString();

        String resultFilter = cbxResultFilter == null || cbxResultFilter.getSelectedItem() == null
                ? ALL_RESULTS
                : cbxResultFilter.getSelectedItem().toString();

        String keyword = txtSearchStudent == null ? "" : txtSearchStudent.getText().trim();
        if (SEARCH_PLACEHOLDER.equalsIgnoreCase(keyword)) keyword = "";

        return "Lớp: " + classFilter
                + " | Kết quả: " + resultFilter
                + " | Từ khóa: " + (keyword.isEmpty() ? "Không có" : keyword);
    }

    private String getExcelStyleForCell(int col, Object value) {
        if (col == 4) return "Score";

        if (col == 6) {
            String text = value == null ? "" : value.toString();
            return "ĐẠT".equals(text) ? "Pass" : "Fail";
        }

        if (col == 0 || col == 1 || col >= 4) {
            return "Center";
        }

        return "Cell";
    }

    private boolean isNumericColumn(int col, Object value) {
        if (value == null) return false;
        if (col != 0 && col != 4) return false;

        try {
            Double.parseDouble(value.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatExcelValue(Object value, int col) {
        if (value == null) return "";

        String text = value.toString();

        if (col == 4) {
            try {
                return String.format(java.util.Locale.US, "%.2f", Double.parseDouble(text));
            } catch (Exception ignored) {
                return text;
            }
        }

        return text;
    }

    private String borderXml() {
        return "<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>"
                + "<Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>"
                + "<Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>"
                + "<Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>";
    }

    private String xmlEscape(String text) {
        if (text == null) return "";

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) lblStatus.setText(message);
        if (btnRefresh != null) btnRefresh.setEnabled(!loading);
        if (btnExport != null) btnExport.setEnabled(!loading);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private static class ReportCellRenderer extends DefaultTableCellRenderer {
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

            if (column == 0 || column == 1 || column >= 4) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", column == 1 ? Font.BOLD : Font.PLAIN, 13));
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            return label;
        }
    }

    private static class ScoreRenderer extends DefaultTableCellRenderer {
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

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
            } else {
                label.setBackground(new Color(255, 251, 235));
                label.setForeground(new Color(120, 53, 15));
            }

            return label;
        }
    }

    private static class ClassificationRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String text = value == null ? "" : value.toString();
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 8, 5, 8));

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
                return label;
            }

            String lower = text.toLowerCase();

            if (lower.contains("yếu") || lower.contains("kém")) {
                label.setBackground(RED_SOFT);
                label.setForeground(RED);
            } else if (lower.contains("trung")) {
                label.setBackground(ORANGE_SOFT);
                label.setForeground(ORANGE);
            } else {
                label.setBackground(GREEN_SOFT);
                label.setForeground(GREEN);
            }

            return label;
        }
    }

    private static class ResultBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String text = value == null ? "" : value.toString();
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 8, 5, 8));

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
                return label;
            }

            if ("ĐẠT".equals(text)) {
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

    private static class StudentIconPanel extends MetricIconPanel {
        StudentIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.fillOval(13, 12, 7, 7);
            g2.fillOval(23, 12, 7, 7);
            g2.drawArc(10, 22, 13, 12, 0, 180);
            g2.drawArc(21, 22, 13, 12, 0, 180);
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

    private static class FailIconPanel extends MetricIconPanel {
        FailIconPanel(Color accent, Color soft) { super(accent, soft); }

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
        JFrame f = new JFrame("Gói 6 - Báo Cáo Học Tập");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 750);
        f.setLocationRelativeTo(null);
        f.add(new StudyReportPanel());
        f.setVisible(true);
    }
}
