package com.mycompany.myapp.utils;

import com.mycompany.myapp.model.PermissionDTO;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PermissionManager {

    private static final Map<String, PermissionDTO> permissionMap = new HashMap<>();

    private PermissionManager() {
    }

    public static void setPermissions(List<PermissionDTO> permissions) {
        permissionMap.clear();

        System.out.println("===== PERMISSIONS LOADED =====");

        if (permissions == null) {
            System.out.println("permissions = null");
            return;
        }

        for (PermissionDTO p : permissions) {
            if (p == null || p.functionName == null) {
                continue;
            }

            putPermission(p.functionName, p);
            putPermission(normalize(p.functionName), p);

            // Đăng ký thêm alias để Java gọi theo mã chức năng vẫn khớp DB,
            // kể cả DB đang lưu tên tiếng Việt hoặc tên chưa chuẩn hóa.
            registerAliases(p);

            System.out.println(
                    normalize(p.functionName)
                            + " | view=" + p.canView
                            + " | add=" + p.canAdd
                            + " | edit=" + p.canEdit
                            + " | delete=" + p.canDelete
            );
        }

        System.out.println("TOTAL PERMISSION KEYS = " + permissionMap.size());
    }

    private static void putPermission(String key, PermissionDTO permission) {
        if (key == null || permission == null) {
            return;
        }

        String normalizedKey = normalize(key);
        if (!normalizedKey.isEmpty()) {
            permissionMap.put(normalizedKey, permission);
        }
    }

    private static void registerAliases(PermissionDTO p) {
        String key = normalize(p.functionName);

        // QUẢN TRỊ
        if (containsAny(key, "TAI_KHOAN", "ACCOUNT", "USER")) {
            putPermission("QUAN_LY_TAI_KHOAN", p);
            putPermission("ACCOUNT_MGR", p);
        }

        if (containsAny(key, "HE_THONG", "SYSTEM", "CAU_HINH")) {
            putPermission("QUAN_LY_HE_THONG", p);
            putPermission("SYSTEM_CFG", p);
        }

        // HỌC VỤ / ĐÀO TẠO
        if (containsAny(key, "HOC_VIEN", "STUDENT")) {
            putPermission("QUAN_LY_HOC_VIEN", p);
            putPermission("GV_STUDENT_MGR", p);
        }

        if (containsAny(key, "MON_HOC", "KHOA_HOC", "SUBJECT", "COURSE")) {
            putPermission("QUAN_LY_MON_HOC", p);
            putPermission("QUAN_LY_KHOA_HOC", p);
            putPermission("GV_SUBJECT", p);
        }

        if (containsAny(key, "LOP_HOC", "CLASS")) {
            putPermission("QUAN_LY_LOP_HOC", p);
            putPermission("GV_CLASS_MGR", p);
            putPermission("LOP_DUOC_PHAN_CONG", p);
        }

        if (containsAny(key, "XEP_LOP", "PLACEMENT", "ENROLL")) {
            putPermission("XEP_LOP_HOC_VIEN", p);
            putPermission("GV_ENROLL", p);
        }

        if (containsAny(key, "LICH", "SCHEDULE", "THOI_KHOA_BIEU")) {
            putPermission("QUAN_LY_LICH_BIEU", p);
            putPermission("LICH_DAY_GIAO_VIEN", p);
            putPermission("GV_SCHEDULE", p);
        }

        if (containsAny(key, "PHAN_CONG", "ASSIGN")) {
            putPermission("PHAN_CONG_GIAO_VIEN", p);
            putPermission("GV_ASSIGN", p);
        }

        // GIẢNG DẠY / HỌC TẬP
        if (containsAny(key, "HOC_TAP", "ACADEMIC", "DASHBOARD", "GIANG_DAY")) {
            putPermission("QUAN_LY_HOC_TAP", p);
            putPermission("DASHBOARD_GIAO_VIEN", p);
            putPermission("HO_SO_CA_NHAN", p);
        }

        if (containsAny(key, "DIEM_DANH", "ATTENDANCE")) {
            putPermission("DIEM_DANH", p);
            putPermission("BAO_CAO_DIEM_DANH", p);
            putPermission("ATTENDANCE", p);
            putPermission("ATTENDANCE_ANA", p);
        }

        if (containsAny(key, "NHAP_DIEM", "GRADE")) {
            putPermission("NHAP_DIEM", p);
            putPermission("GRADE_ENTRY", p);
        }

        if (containsAny(key, "TONG_KET", "KET_QUA", "RESULT", "REPORT", "BAO_CAO")) {
            putPermission("TONG_KET_DIEM", p);
            putPermission("BAO_CAO_HOC_TAP", p);
            putPermission("ACADEMIC_RESULT", p);
            putPermission("GV_REPORT", p);
        }

        // TÀI CHÍNH / KẾ TOÁN
        if (containsAny(key, "HOC_PHI", "TAI_CHINH", "FINANCE", "INVOICE", "THANH_TOAN", "PAYMENT")) {
            putPermission("QUAN_LY_HOC_PHI_TAI_CHINH", p);
            putPermission("QUAN_LY_HOC_PHI", p);
            putPermission("GHI_NHAN_THANH_TOAN", p);
            putPermission("TRA_CUU_HOC_PHI", p);
            putPermission("HOA_DON_DIEN_TU", p);
            putPermission("FIN_PAYMENT", p);
            putPermission("FIN_MANAGE", p);
            putPermission("FIN_LOOKUP", p);
            putPermission("FIN_ISSUE", p);
        }

        if (containsAny(key, "LUONG", "PAYROLL", "SALARY", "NHAN_SU")) {
            putPermission("BANG_LUONG", p);
            putPermission("FIN_PAYROLL", p);
        }
    }

    public static boolean canView(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return isSystemAdmin() || (p != null && p.canView) || allowByRoleFallback(moduleCode, "VIEW");
    }

    public static boolean canAdd(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return isSystemAdmin() || (p != null && p.canAdd) || allowByRoleFallback(moduleCode, "ADD");
    }

    public static boolean canEdit(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return isSystemAdmin() || (p != null && p.canEdit) || allowByRoleFallback(moduleCode, "EDIT");
    }

    public static boolean canDelete(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return isSystemAdmin() || (p != null && p.canDelete) || allowByRoleFallback(moduleCode, "DELETE");
    }

    public static PermissionDTO get(String moduleCode) {
        if (moduleCode == null) {
            return null;
        }

        return permissionMap.get(normalize(moduleCode));
    }

    public static boolean hasAnyPermission() {
        if (!permissionMap.isEmpty()) {
            return true;
        }

        return getRoleText().length() > 0;
    }

    public static void clear() {
        permissionMap.clear();
    }

    private static boolean allowByRoleFallback(String moduleCode, String action) {
        String code = normalize(moduleCode);

        if (code.isEmpty()) {
            return false;
        }

        String roleText = getRoleText();

        if (roleText.isEmpty()) {
            return false;
        }

        // Fallback này dùng để tránh lỗi toàn bộ giao diện bị chặn
        // khi DB đã gán role nhưng FUNCTION.name_function không khớp mã module Java.
        // Quyền CRUD vẫn ưu tiên dữ liệu trong DB nếu map được.
        if (hasAnyRole(roleText, "ACCOUNTANT", "KE_TOAN", "NHAN_VIEN_KE_TOAN", "NHAN_VIEN_KE_TOAN")) {
            return containsAny(code,
                    "HOC_PHI", "TAI_CHINH", "THANH_TOAN", "PAYMENT", "INVOICE",
                    "HOA_DON", "LUONG", "PAYROLL", "FIN_"
            );
        }

        if (hasAnyRole(roleText, "ACADEMIC_STAFF", "GIAO_VU", "NHAN_VIEN_GIAO_VU", "NHAN_VIEN_QUAN_LY_NGHIEP_VU")) {
            return containsAny(code,
                    "HOC_VIEN", "HOC_TAP", "MON_HOC", "KHOA_HOC", "LOP_HOC",
                    "XEP_LOP", "LICH", "SCHEDULE", "PHAN_CONG", "DIEM_DANH", "BAO_CAO", "GV_"
            );
        }

        if (hasAnyRole(roleText, "TEACHER", "GIAO_VIEN")) {
            return containsAny(code,
                    "DASHBOARD", "GIANG_DAY", "LICH_DAY", "SCHEDULE", "LOP_DUOC_PHAN_CONG",
                    "DIEM_DANH", "ATTENDANCE", "NHAP_DIEM", "GRADE", "TONG_KET",
                    "ACADEMIC", "HO_SO_CA_NHAN", "HOC_TAP"
            );
        }

        return false;
    }

    private static boolean isSystemAdmin() {
        String roleText = getRoleText();

        return hasAnyRole(
                roleText,
                "ADMIN",
                "QUAN_TRI_HE_THONG",
                "NHAN_VIEN_QUAN_LY_HE_THONG"
        );
    }

    private static String getRoleText() {
        try {
            List<String> roles = SessionStore.getUserRoles();

            if (roles == null || roles.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();

            for (String role : roles) {
                if (role != null) {
                    sb.append(normalize(role)).append("|");
                }
            }

            return sb.toString();
        } catch (Throwable ex) {
            return "";
        }
    }

    private static boolean hasAnyRole(String roleText, String... roleNames) {
        if (roleText == null || roleNames == null) {
            return false;
        }

        for (String roleName : roleNames) {
            if (roleText.contains(normalize(roleName))) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsAny(String value, String... tokens) {
        if (value == null || tokens == null) {
            return false;
        }

        for (String token : tokens) {
            if (token != null && value.contains(normalize(token))) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String noAccent = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return noAccent
                .toUpperCase(Locale.ROOT)
                .replace('Đ', 'D')
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}
