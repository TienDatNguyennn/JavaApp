package com.mycompany.myapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String USERNAME = "quanlytrungtam";
    private static final String PASSWORD = "Admin123";

    private static final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection txConn = transactionConnection.get();

        if (txConn != null && !txConn.isClosed()) {
            return txConn;
        }

        try {
            Class.forName("oracle.jdbc.OracleDriver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found.", e);
        }
    }

    public static void beginTransaction() throws SQLException {
        Connection conn = transactionConnection.get();

        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                conn.setAutoCommit(true);
                transactionConnection.set(conn);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found.", e);
            }
        }
    }

    public static void commitTransaction() throws SQLException {
        Connection conn = transactionConnection.get();

        if (conn == null) return;

        try {
            conn.commit();
            conn.setAutoCommit(true);
        } finally {
            conn.close();
            transactionConnection.remove();
        }
    }

    public static void rollbackTransaction() {
        Connection conn = transactionConnection.get();

        if (conn == null) return;

        try {
            if (!conn.isClosed()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            transactionConnection.remove();
        }
    }

    public static Connection getNewConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found.", e);
        }
    }

    public static Connection getNewConnectionReadCommitted() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found.", e);
        }
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed() && transactionConnection.get() != conn) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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