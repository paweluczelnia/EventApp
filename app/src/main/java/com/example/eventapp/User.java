package com.example.eventapp;

import java.io.Serializable;

public class User implements Serializable {

    public String Login, Email, Phone;

    public User()
    {
    }

    public User(String login, String email, String phone)
    {
        Login = login;
        Email = email;
        Phone = phone;
    }
}
