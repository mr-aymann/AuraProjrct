package com.example.aura_project.Model;

public class User {

    private String email,password,name,phone;

    public User()
    {
    }
    public User(String email,String password,String name,String phone){

        this.email=email;
        this.name=name;
        this.password=password;
        this.phone=phone;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }
}
