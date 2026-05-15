/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // 1. Khai báo thông tin kết nối
    // NẾU DÙNG ORACLE 11g (Bản XE): "jdbc:oracle:thin:@localhost:1521:XE"
    // NẾU DÙNG ORACLE 19c/21c: "jdbc:oracle:thin:@localhost:1521/XEPDB1" hoặc "ORCLPDB1"
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    //private static final String URL = "jdbc:oracle:thin:@localhost:1521/ORCLPDB";
    private static final String USERNAME = "quanlytrungtam"; // ĐỔI LẠI TÊN USER CỦA CẬU
    private static final String PASSWORD = "Admin123"; // ĐỔI LẠI MẬT KHẨU CỦA CẬU

    // 2. Private constructor: Ngăn không cho ai dùng từ khóa 'new' tạo object này
    private DBConnection() {
    }

    // 3. Method lấy Connection
    public static Connection getConnection() throws SQLException {
        try {
            // Với JDBC bản mới (từ Java 8 trở lên), dòng Class.forName này không còn bắt buộc
            // Nhưng thêm vào để chắc chắn Driver Oracle được load vào bộ nhớ.
            Class.forName("oracle.jdbc.OracleDriver");
            
            // Gọi DriverManager để xin kết nối
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found. Hãy kiểm tra lại file ojdbc.jar", e);
        }
    }

    // 4. Khối Main này CHỈ ĐỂ TEST NHANH, code thật sẽ gọi từ Repository
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Kết nối Oracle Database THÀNH CÔNG!");
                System.out.println("Phiên bản DB: " + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("Kết nối THẤT BẠI. Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}