/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

public class SessionStore {
    private static String currentToken;

    public static void setCurrentToken(String token) {
        currentToken = token;
    }

    public static String getCurrentToken() {
        return currentToken;
    }
    
    public static void clearSession() {
        currentToken = null;
    }
}