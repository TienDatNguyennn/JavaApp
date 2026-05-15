/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.exception;

/**
 *
 * @author Tien Dat
 */

public class DuplicateDataException extends RuntimeException {
    private String errorField;

    public DuplicateDataException(String errorField, String message) {
        super(message);
        this.errorField = errorField;
    }

    public String getErrorField() {
        return errorField;
    }
}