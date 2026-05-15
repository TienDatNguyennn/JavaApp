package com.mycompany.myapp.utils;

public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    private Result(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    // Trả về khi thao tác thành công
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, message, data, null);
    }

    // Trả về khi có lỗi xảy ra
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, message, null, null);
    }
    
    // Trả về khi có lỗi kèm mã lỗi cụ thể (dành cho việc mở rộng sau này)
    public static <T> Result<T> failure(String errorCode, String message) {
        return new Result<>(false, message, null, errorCode);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getErrorCode() { return errorCode; }
}