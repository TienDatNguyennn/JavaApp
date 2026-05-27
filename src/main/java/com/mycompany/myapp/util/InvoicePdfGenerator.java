package com.mycompany.myapp.util;

import com.mycompany.myapp.model.Invoice;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Xuất hóa đơn điện tử ra file PDF (A4).
 * Font: Arial từ Windows nếu có, fallback Helvetica (mất dấu tiếng Việt).
 */
public class InvoicePdfGenerator {

    private static final float W       = PDRectangle.A4.getWidth();   // 595.3
    private static final float H       = PDRectangle.A4.getHeight();  // 841.9
    private static final float MARGIN  = 50f;
    private static final float CONT_W  = W - 2 * MARGIN;
    private static final float VAL_X   = MARGIN + 160f;  // start x of label-value pairs

    private final PDFont  bold;
    private final PDFont  reg;
    private final boolean needsNormalize; // true when fallback Latin font is used
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ── Public entry point ────────────────────────────────────────────
    public static void exportToStream(Invoice inv,
                                      String buyerName, String taxCode,
                                      String address,   String email,
                                      String invoiceType,
                                      OutputStream out) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDFont boldFont = loadFont(doc, true);
            PDFont regFont  = loadFont(doc, false);
            new InvoicePdfGenerator(boldFont, regFont)
                    .build(doc, inv, buyerName, taxCode, address, email, invoiceType);
            doc.save(out);
        }
    }

    // ── Constructor ───────────────────────────────────────────────────
    private InvoicePdfGenerator(PDFont bold, PDFont reg) {
        this.bold = bold;
        this.reg  = reg;
        this.needsNormalize = (reg instanceof PDType1Font);
    }

    // ── PDF builder ───────────────────────────────────────────────────
    private void build(PDDocument doc, Invoice inv,
                       String buyerName, String taxCode,
                       String address,   String email,
                       String invoiceType) throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy");
        String issuedDate = inv.getCreatedAt() != null
                ? dateFmt.format(inv.getCreatedAt())
                : dateFmt.format(new Date());

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = H - MARGIN;

            // ── Company header ────────────────────────────────────
            y = centered(cs, bold, 14, "TRUNG TÂM ĐÀO TẠO", y);
            y = centered(cs, reg,  10, "Địa chỉ: 123 Đường ABC, TP. Hồ Chí Minh  |  ĐT: 028.1234.5678", y - 2);
            y = centered(cs, reg,  10, "Email: info@trungtam.edu.vn  |  Website: www.trungtam.edu.vn", y - 2);
            y -= 8;
            hLine(cs, y, 1.5f);
            y -= 18;

            // ── Invoice title ─────────────────────────────────────
            y = centered(cs, bold, 18, "HÓA ĐƠN ĐIỆN TỬ", y);
            y = centered(cs, reg,  11, invoiceType, y - 4);
            y -= 4;
            y = centered(cs, reg, 11,
                    "Số: INV-" + String.format("%03d", inv.getInvoiceId())
                    + "      Ngày: " + issuedDate, y - 2);
            y -= 10;
            hLine(cs, y, 0.5f);
            y -= 18;

            // ── Buyer info ────────────────────────────────────────
            y = labelValue(cs, y, "Tên người mua:",        fill(buyerName));
            y = labelValue(cs, y, "Mã số thuế:",           fill(taxCode));
            y = labelValue(cs, y, "Địa chỉ:",              fill(address));
            y = labelValue(cs, y, "Email:",                 fill(email));
            y = labelValue(cs, y, "Hình thức thanh toán:", fill(inv.getPaymentMethod()));
            y -= 6;
            hLine(cs, y, 0.5f);
            y -= 18;

            // ── Items table ───────────────────────────────────────
            float c0 = MARGIN, c1 = MARGIN + 30, c2 = W - MARGIN - 120;
            at(cs, bold, 11, "STT",                        c0, y);
            at(cs, bold, 11, "Tên dịch vụ / hàng hóa",   c1, y);
            at(cs, bold, 11, "Thành tiền",                 c2, y);
            y -= 6;
            hLine(cs, y, 0.5f);
            y -= 16;

            at(cs,    reg, 11, "1",                        c0, y);
            at(cs,    reg, 11, "Học phí khóa học",         c1, y);
            rightAt(cs, reg, 11, nf.format(inv.getTotalAmount()) + "đ", W - MARGIN, y);
            y -= 18;
            hLine(cs, y, 0.5f);
            y -= 14;

            // ── Totals ────────────────────────────────────────────
            float lx = W - MARGIN - 185, vx = W - MARGIN;
            at(cs, reg, 11, "Tổng tiền hàng:", lx, y);
            rightAt(cs, reg, 11, nf.format(inv.getTotalAmount()) + "đ", vx, y);
            y -= 15;

            if (inv.getDiscountAmt() > 0) {
                at(cs, reg, 11, "Chiết khấu:", lx, y);
                rightAt(cs, reg, 11, "-" + nf.format(inv.getDiscountAmt()) + "đ", vx, y);
                y -= 15;
            }

            hLine(cs, lx, y, vx - lx, 0.5f);
            y -= 14;
            at(cs, bold, 12, "Tổng thanh toán:", lx, y);
            rightAt(cs, bold, 12, nf.format(inv.getFinalAmount()) + "đ", vx, y);
            y -= 22;
            hLine(cs, y, 1f);
            y -= 18;

            // ── Status ────────────────────────────────────────────
            String statusDisplay = "SENT".equals(inv.getApiStatus())     ? "ĐÃ PHÁT HÀNH"  :
                                   "ADJUSTED".equals(inv.getApiStatus()) ? "ĐÃ ĐIỀU CHỈNH" : "CHỜ XỬ LÝ";
            y = labelValue(cs, y, "Trạng thái hóa đơn:", statusDisplay);
            y -= 30;

            // ── Signature area ────────────────────────────────────
            float s1cx = MARGIN  + 70;
            float s2cx = W - MARGIN - 80;
            centeredAt(cs, bold, 11, "Người mua hàng",          s1cx, y);
            centeredAt(cs, bold, 11, "Người bán hàng",          s2cx, y);  y -= 14;
            centeredAt(cs, reg,  10, "(Ký, họ tên)",            s1cx, y);
            centeredAt(cs, reg,  10, "(Ký, đóng dấu, họ tên)", s2cx, y);

            // ── Footer ────────────────────────────────────────────
            hLine(cs, 30f, 0.5f);
            centeredAt(cs, reg, 9,
                    "Hóa đơn được tạo tự động — " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                    W / 2, 18);
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────

    /** Draw centered text, return next y (below the drawn text). */
    private float centered(PDPageContentStream cs, PDFont font, float size,
                           String text, float y) throws IOException {
        String s = t(text);
        float tw = font.getStringWidth(s) / 1000f * size;
        at(cs, font, size, s, (W - tw) / 2f, y);
        return y - size - 2;
    }

    private void centeredAt(PDPageContentStream cs, PDFont font, float size,
                             String text, float cx, float y) throws IOException {
        String s = t(text);
        float tw = font.getStringWidth(s) / 1000f * size;
        at(cs, font, size, s, cx - tw / 2f, y);
    }

    private void at(PDPageContentStream cs, PDFont font, float size,
                    String text, float x, float y) throws IOException {
        String s = t(text);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(s);
        cs.endText();
    }

    private void rightAt(PDPageContentStream cs, PDFont font, float size,
                          String text, float rightX, float y) throws IOException {
        String s = t(text);
        float tw = font.getStringWidth(s) / 1000f * size;
        at(cs, font, size, s, rightX - tw, y);
    }

    /** Draw bold label + regular value on same line; return next y. */
    private float labelValue(PDPageContentStream cs, float y,
                              String label, String value) throws IOException {
        at(cs, bold, 11, label, MARGIN, y);
        at(cs, reg,  11, value, VAL_X,  y);
        return y - 16;
    }

    /** Full-width horizontal line. */
    private void hLine(PDPageContentStream cs, float y, float w) throws IOException {
        hLine(cs, MARGIN, y, CONT_W, w);
    }

    /** Partial horizontal line starting at x. */
    private void hLine(PDPageContentStream cs, float x, float y,
                        float width, float lineW) throws IOException {
        cs.setLineWidth(lineW);
        cs.moveTo(x, y);
        cs.lineTo(x + width, y);
        cs.stroke();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /** Normalize Vietnamese if fallback Latin font is in use. */
    private String t(String s) {
        if (s == null) return "";
        if (!needsNormalize) return s;
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "")
                  .replace('đ', 'd').replace('Đ', 'D');
    }

    private static String fill(String s) {
        return (s == null || s.isBlank()) ? "—" : s.trim();
    }

    private static PDFont loadFont(PDDocument doc, boolean bold) {
        String[] paths = bold
                ? new String[]{"C:/Windows/Fonts/arialbd.ttf", "C:/Windows/Fonts/arial.ttf"}
                : new String[]{"C:/Windows/Fonts/arial.ttf"};
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                try { return PDType0Font.load(doc, f); }
                catch (IOException ignored) {}
            }
        }
        return bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
    }
}
