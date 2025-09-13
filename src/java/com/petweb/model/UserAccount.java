package com.petweb.model;

public class UserAccount {
    private int id;
    private String userName;
    private String gender;
    private String password;
    private String email;
    private String phone;
    private String address;
    private byte[] avatar;
    private String fullName;
    private String role;

    // Constructor không tham số
    public UserAccount() {}

    // Constructor đầy đủ
    public UserAccount(int id, String userName, String gender, String password, String email,
                       String phone, String address, byte[] avatar, String fullName,String role) {
        this.id = id;
        this.userName = userName;
        this.gender = gender;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.avatar = avatar;
        this.fullName = fullName;
        this.role = role;
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
