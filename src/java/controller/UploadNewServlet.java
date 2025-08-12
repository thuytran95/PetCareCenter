/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@WebServlet(name="UploadServlet",urlPatterns={"/upload"})
@MultipartConfig
public class UploadNewServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            Part filePart = request.getPart("file");
            BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while((line=reader.readLine()) != null){
                content.append(line).append("</br>");
            }
            reader.close();
            request.setAttribute("content", content.toString());
            request.getRequestDispatcher("result.jsp").forward(request, response);
        }
    }

   


