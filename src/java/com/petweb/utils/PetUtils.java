/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.utils;

import com.petweb.model.Pet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Duyet
 */
public class PetUtils {
    public List<Pet> getAllPets() throws ClassNotFoundException {
    List<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pet"; // tên bảng pet

        try (Connection conn = ConnectionUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pet pet = new Pet();
                pet.setPetId(rs.getInt("pet_id"));
                pet.setName(rs.getString("name"));
                pet.setSpecies(rs.getString("species"));
                pet.setBreed(rs.getString("breed"));
                pet.setGender(rs.getString("gender"));
                pet.setDob(rs.getDate("dob").toLocalDate());
                pet.setFurColor(rs.getString("furColor"));
                pet.setIdentifyingMarks(rs.getString("identifyingMarks"));
                pet.setPhoto(rs.getBytes("photo")); // BLOB
                list.add(pet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
