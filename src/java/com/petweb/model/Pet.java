package com.petweb.model;

import java.time.LocalDate;

public class Pet {
    private int petId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private LocalDate dob;
    private String furColor;
    private String identifyingMarks;
    private byte[] photo;
    private String owner;
    private int userId;

    // Constructor rỗng
    public Pet() {}

    // Constructor đầy đủ
    public Pet(int petId, String name, String species, String breed, String gender,
               LocalDate dob, String furColor, String identifyingMarks,
               byte[] photo, String owner, int userId) {
        this.petId = petId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.dob = dob;
        this.furColor = furColor;
        this.identifyingMarks = identifyingMarks;
        this.photo = photo;
        this.owner = owner;
        this.userId = userId;
    }

    // Getter & Setter
    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getFurColor() { return furColor; }
    public void setFurColor(String furColor) { this.furColor = furColor; }

    public String getIdentifyingMarks() { return identifyingMarks; }
    public void setIdentifyingMarks(String identifyingMarks) { this.identifyingMarks = identifyingMarks; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
