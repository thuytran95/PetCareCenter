package com.petweb.test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.sql.Connection;
import com.petweb.utils.ConnectionUtils;
/**
 *
 * @author Duyet
 */
public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = ConnectionUtils.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("ket noi thanh cong");
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("ket noi that bai " + e.getMessage());
        }
    }
}
