package com.mycompany.myapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionStore {

    private static String token;
    private static int accountId;
    private static int userId;
    private static String username;
    private static String fullName;
    private static List<String> userRoles = new ArrayList<>();

    private SessionStore() {
    }

    public static void saveSession(
            String tokenValue,
            Map<String, Object> userInfo,
            List<String> roles
    ) {
        token = tokenValue;

        accountId = getInt(userInfo.get("account_id"));
        userId = getInt(userInfo.get("user_id"));

        username = getString(userInfo.get("username"));
        fullName = getString(userInfo.get("full_name"));

        userRoles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);

        System.out.println("===== SESSION SAVED =====");
        System.out.println("Session accountId = " + accountId);
        System.out.println("Session userId = " + userId);
        System.out.println("Session username = " + username);
        System.out.println("Session fullName = " + fullName);
        System.out.println("Session roles = " + userRoles);
    }

    public static String getToken() {
        return token;
    }

    public static int getAccountId() {
        return accountId;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static List<String> getUserRoles() {
        return userRoles == null ? new ArrayList<>() : new ArrayList<>(userRoles);
    }

    public static void clearSession() {
        token = null;
        accountId = 0;
        userId = 0;
        username = null;
        fullName = null;
        userRoles = new ArrayList<>();
    }

    private static int getInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getString(Object value) {
        return value == null ? "" : value.toString();
    }
}