package com.example.foodorderingfyp.ModelClass;

public class Admins {
    private String name,phone,password,isAdmin;

    public Admins(){

    }

    public Admins(String name, String phone, String password, String isAdmin) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

}
