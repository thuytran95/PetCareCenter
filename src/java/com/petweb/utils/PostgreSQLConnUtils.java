/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Duyet
 */
public class PostgreSQLConnUtils {
    public static Connection getPostgreSQLConnection()
            throws ClassNotFoundException, SQLException {
        String hostName = "localhost";
        String dbName = "petcare";
        String userName = "postgres";
        String password = "1";
        return getPostgreSQLConnection(hostName, dbName, userName, password);
    }

    public static Connection getPostgreSQLConnection(String hostName, String dbName,
                                                     String userName, String password)
            throws SQLException, ClassNotFoundException {

        // Load Driver PostgreSQL
        Class.forName("org.postgresql.Driver");

        // Cấu trúc URL kết nối cho PostgreSQL
        // Ví dụ: jdbc:postgresql://localhost:5432/mytest
        String connectionURL = "jdbc:postgresql://" + hostName + ":5432/" + dbName;

        Connection conn = DriverManager.getConnection(connectionURL, userName, password);
        return conn;
    }
}
