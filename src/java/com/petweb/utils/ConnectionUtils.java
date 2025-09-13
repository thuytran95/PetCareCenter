/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.utils;
import java.sql.Connection;
import java.sql.SQLException;
/**
 *
 * @author Duyet
 */
public class ConnectionUtils {
       public static Connection getConnection() 
            throws ClassNotFoundException, SQLException {

        // Kết nối PostgreSQL
        return PostgreSQLConnUtils.getPostgreSQLConnection();
    }

    public static void closeQuietly(Connection conn) {
        try {
            conn.close();
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            // Ignore
        }
    }
}
