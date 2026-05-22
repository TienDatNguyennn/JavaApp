package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PromotionRule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng PROMOTION_RULE.
 * Caller phải gọi DBConnection.commitTransaction() sau mỗi write.
 */
public class PromotionDAO {

    public List<PromotionRule> findAllActive() throws SQLException {
        List<PromotionRule> list = new ArrayList<>();
        String sql = "SELECT promo_id, promo_name, discount_rate, min_subjects, " +
                     "created_at, updated_at, is_deleted " +
                     "FROM PROMOTION_RULE WHERE is_deleted = 0 ORDER BY promo_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public PromotionRule findById(int promoId) throws SQLException {
        String sql = "SELECT promo_id, promo_name, discount_rate, min_subjects, " +
                     "created_at, updated_at, is_deleted " +
                     "FROM PROMOTION_RULE WHERE promo_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean insert(PromotionRule p) throws SQLException {
        String sql = "INSERT INTO PROMOTION_RULE (promo_id, promo_name, discount_rate, min_subjects) " +
                     "VALUES ((SELECT NVL(MAX(promo_id), 0) + 1 FROM PROMOTION_RULE), ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, p.getPromoName());
            ps.setDouble(2, p.getDiscountRate());
            ps.setInt(3, p.getMinSubjects());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(PromotionRule p) throws SQLException {
        String sql = "UPDATE PROMOTION_RULE " +
                     "SET promo_name = ?, discount_rate = ?, min_subjects = ?, " +
                     "    updated_at = SYSDATE " +
                     "WHERE promo_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, p.getPromoName());
            ps.setDouble(2, p.getDiscountRate());
            ps.setInt(3, p.getMinSubjects());
            ps.setInt(4, p.getPromoId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int promoId) throws SQLException {
        String sql = "UPDATE PROMOTION_RULE SET is_deleted = 1, updated_at = SYSDATE " +
                     "WHERE promo_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            return ps.executeUpdate() > 0;
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
