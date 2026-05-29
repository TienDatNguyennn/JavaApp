package com.mycompany.myapp.utils;

import javax.swing.*;
import java.awt.*;

public class PermissionUIHelper {

    private PermissionUIHelper() {
    }

    public static void applyCrudPermission(
            String moduleCode,
            JButton btnAdd,
            JButton btnEdit,
            JButton btnDelete
    ) {
        applyButtonPermission(
                btnAdd,
                PermissionManager.canAdd(moduleCode),
                "Bạn không có quyền thêm dữ liệu ở chức năng này."
        );

        applyButtonPermission(
                btnEdit,
                PermissionManager.canEdit(moduleCode),
                "Bạn không có quyền sửa dữ liệu ở chức năng này."
        );

        applyButtonPermission(
                btnDelete,
                PermissionManager.canDelete(moduleCode),
                "Bạn không có quyền xóa dữ liệu ở chức năng này."
        );
    }

    public static boolean requireView(Component parent, String moduleCode, String screenName) {
        if (!PermissionManager.canView(moduleCode)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Bạn không có quyền xem chức năng: " + screenName,
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    public static boolean requireAdd(Component parent, String moduleCode) {
        if (!PermissionManager.canAdd(moduleCode)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Bạn không có quyền thêm dữ liệu ở chức năng này.",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    public static boolean requireEdit(Component parent, String moduleCode) {
        if (!PermissionManager.canEdit(moduleCode)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Bạn không có quyền sửa dữ liệu ở chức năng này.",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    public static boolean requireDelete(Component parent, String moduleCode) {
        if (!PermissionManager.canDelete(moduleCode)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Bạn không có quyền xóa dữ liệu ở chức năng này.",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    public static boolean checkView(String moduleCode) {
        return PermissionManager.canView(moduleCode);
    }

    public static boolean checkAdd(String moduleCode) {
        return PermissionManager.canAdd(moduleCode);
    }

    public static boolean checkEdit(String moduleCode) {
        return PermissionManager.canEdit(moduleCode);
    }

    public static boolean checkDelete(String moduleCode) {
        return PermissionManager.canDelete(moduleCode);
    }

    public static void applyActionButton(
            String moduleCode,
            JButton button,
            PermissionType permissionType,
            String deniedTooltip
    ) {
        if (button == null) {
            return;
        }

        boolean allowed = hasPermission(moduleCode, permissionType);
        applyButtonPermission(button, allowed, deniedTooltip);
    }

    public static boolean requireAction(
            Component parent,
            String moduleCode,
            PermissionType permissionType,
            String deniedMessage
    ) {
        if (!hasPermission(moduleCode, permissionType)) {
            JOptionPane.showMessageDialog(
                    parent,
                    deniedMessage,
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    private static void applyButtonPermission(
            JButton button,
            boolean allowed,
            String deniedTooltip
    ) {
        if (button == null) {
            return;
        }

        button.setEnabled(allowed);

        if (allowed) {
            button.setToolTipText(null);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            button.setToolTipText(deniedTooltip);
            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private static boolean hasPermission(String moduleCode, PermissionType permissionType) {
        if (permissionType == null) {
            return false;
        }

        switch (permissionType) {
            case VIEW:
                return PermissionManager.canView(moduleCode);
            case ADD:
                return PermissionManager.canAdd(moduleCode);
            case EDIT:
                return PermissionManager.canEdit(moduleCode);
            case DELETE:
                return PermissionManager.canDelete(moduleCode);
            default:
                return false;
        }
    }

    public enum PermissionType {
        VIEW,
        ADD,
        EDIT,
        DELETE
    }
}
