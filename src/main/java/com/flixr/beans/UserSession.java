package com.flixr.beans;

/**
 * @author Thomas Thompson
 * Tracks User Sessions
 */

public class UserSession {

    private String email;
    private int user_id;
    private boolean status; // logged in = true, logged out = false

    public UserSession(User user) {
        this.email =  user.getEmail();
        this.user_id = user.getUserID();
        this.status = true; // logged in
    }

    public UserSession(String email, int user_id) {
        this.email =  email;
        this.user_id = user_id;
        this.status = true; // logged in
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getStatus() {
        if (status) return "on";
        else return "off";
    }
}
