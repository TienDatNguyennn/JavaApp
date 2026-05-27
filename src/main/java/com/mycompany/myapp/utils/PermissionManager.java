package com.mycompany.myapp.utils;

import com.mycompany.myapp.model.PermissionDTO;

import java.util.HashMap;
import java.util.List;
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
            if (p.functionName == null) {
                continue;
            }

            String key = normalize(p.functionName);
            permissionMap.put(key, p);

            System.out.println(
                    key
                            + " | view=" + p.canView
                            + " | add=" + p.canAdd
                            + " | edit=" + p.canEdit
                            + " | delete=" + p.canDelete
            );
        }
    }

    public static boolean canView(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return p != null && p.canView;
    }

    public static boolean canAdd(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return p != null && p.canAdd;
    }

    public static boolean canEdit(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return p != null && p.canEdit;
    }

    public static boolean canDelete(String moduleCode) {
        PermissionDTO p = get(moduleCode);
        return p != null && p.canDelete;
    }

    public static PermissionDTO get(String moduleCode) {
        if (moduleCode == null) {
            return null;
        }

        return permissionMap.get(normalize(moduleCode));
    }

    public static void clear() {
        permissionMap.clear();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}