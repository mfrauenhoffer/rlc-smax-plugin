package com.microfocus.rlc.plugin.domain;

import com.google.gson.annotations.SerializedName;

public class SmaxAuthenticationRequest {
    @SerializedName("Login")
    private String username;
    @SerializedName("Password")
    private String password;

    public SmaxAuthenticationRequest(String un, String pw) {
        this.username = un;
        this.password = pw;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String val) {
        this.username = val;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String val) {
        this.password = val;
    }
}