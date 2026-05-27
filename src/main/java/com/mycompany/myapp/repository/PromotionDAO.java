package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PromotionRule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng PROMOTION_RULE.
 * Caller phải gọi DBConnection.commitTransaction() sau mỗi thao tác ghi.
 *
 * Bản fix chuẩn thực tế:
 * - Không dùng PROMOTION_RULE_SEQ để tránh ORA-02289 nếu database chưa tạo sequence.
 * - Validate trước insert/update để chặn voucher < 0 hoặc > 100%.
 * - Validate tên chương trình, số môn tối thiểu.
 * - Dùng SELECT NVL(MAX(promo_id),0)+1 theo cấu trúc database hiện tại của project.
 */
public class PromotionDAO {

    private static final double MIN_DISCOUNT_RATE = 0;
    private static final double MAX_DISCOUNT_RATE = 100;
    private static final int MIN_SUBJECTS = 1;
    private static final int MAX_SUBJECTS = 20;
    private static final int MAX_PROMO_NAME_LENGTH = 150;

    public List<PromotionRule> findAllActive() throws SQLException {
        List<PromotionRule> list = new ArrayList<>();

        String sql =
            "SELECT promo_id, promo_name, discount_rate, min_subjects, " +
            "       created_at, updated_at, is_deleted " +
            "FROM PROMOTION_RULE " +
            "WHERE is_deleted = 0 " +
            "ORDER BY promo_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public PromotionRule findById(int promoId) throws SQLException {
        if (promoId <= 0) {
            throw new IllegalArgumentException("Mã chương trình khuyến mãi không hợp lệ.");
        }

        String sql =
            "SELECT promo_id, promo_name, discount_rate, min_subjects, " +
            "       created_at, updated_at, is_deleted " +
            "FROM PROMOTION_RULE " +
            "WHERE promo_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, promoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(PromotionRule p) throws SQLException {
        validatePromotion(p, false);

        String sql =
            "INSERT INTO PROMOTION_RULE " +
            "       (promo_id, promo_name, discount_rate, min_subjects) " +
            "VALUES ((SELECT NVL(MAX(promo_id), 0) + 1 FROM PROMOTION_RULE), ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, p.getPromoName().trim());
            ps.setDouble(2, p.getDiscountRate());
            ps.setInt(3, p.getMinSubjects());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(PromotionRule p) throws SQLException {
        validatePromotion(p, true);

        String sql =
            "UPDATE PROMOTION_RULE " +
            "SET promo_name = ?, " +
            "    discount_rate = ?, " +
            "    min_subjects = ?, " +
            "    updated_at = SYSDATE " +
            "WHERE promo_id = ? " +
            "  AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, p.getPromoName().trim());
            ps.setDouble(2, p.getDiscountRate());
            ps.setInt(3, p.getMinSubjects());
            ps.setInt(4, p.getPromoId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int promoId) throws SQLException {
        if (promoId <= 0) {
            throw new IllegalArgumentException("Mã chương trình khuyến mãi không hợp lệ.");
        }

        String sql =
            "UPDATE PROMOTION_RULE " +
            "SET is_deleted = 1, updated_at = SYSDATE " +
            "WHERE promo_id = ? " +
            "  AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, promoId);
            return ps.executeUpdate() > 0;
        }
    }

    private void validatePromotion(PromotionRule p, boolean isUpdate) {
        if (p == null) {
            throw new IllegalArgumentException("Dữ liệu chương trình khuyến mãi không hợp lệ.");
        }

        if (isUpdate && p.getPromoId() <= 0) {
            throw new IllegalArgumentException("Mã chương trình khuyến mãi không hợp lệ.");
        }

        String promoName = p.getPromoName();

        if (promoName == null || promoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình khuyến mãi không được để trống.");
        }

        if (promoName.trim().length() > MAX_PROMO_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Tên chương trình khuyến mãi không được vượt quá " +
                MAX_PROMO_NAME_LENGTH + " ký tự."
            );
        }

        double discountRate = p.getDiscountRate();

        if (Double.isNaN(discountRate) || Double.isInfinite(discountRate)) {
            throw new IllegalArgumentException("Tỷ lệ khuyến mãi không hợp lệ.");
        }

        if (discountRate < MIN_DISCOUNT_RATE || discountRate > MAX_DISCOUNT_RATE) {
            throw new IllegalArgumentException(
                "Tỷ lệ khuyến mãi phải nằm trong khoảng từ 0% đến 100%."
            );
        }

        if (p.getMinSubjects() < MIN_SUBJECTS || p.getMinSubjects() > MAX_SUBJECTS) {
            throw new IllegalArgumentException(
                "Số môn đăng ký tối thiểu phải nằm trong khoảng từ " +
                MIN_SUBJECTS + " đến " + MAX_SUBJECTS + "."
            );
        }
    }

    private PromotionRule map(ResultSet rs) throws SQLException {
        PromotionRule p = new PromotionRule();

        p.setPromoId(rs.getInt("promo_id"));
        p.setPromoName(rs.getString("promo_name"));
        p.setDiscountRate(rs.getDouble("discount_rate"));
        p.setMinSubjects(rs.getInt("min_subjects"));
        p.setCreatedAt(rs.getDate("created_at"));
        p.setUpdatedAt(rs.getDate("updated_at"));
        p.setDeleted(rs.getInt("is_deleted") == 1);

        return p;
    }
}
