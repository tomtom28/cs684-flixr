package com.flixr.beans;


/**
 * @author Thomas Thompson
 * Wrapper Class to add a login status to a User
 */
public class UserWithStatus extends User {

    private String status;

    public UserWithStatus(User user, boolean isLoggedIn) {

        // Set User fields
        this.setUserID(user.getUserID());
        this.setFullname(user.getFullname());
        this.setPassword(user.getPassword());
        this.setEmail(user.getEmail());
        this.setAge(user.getAge());
        this.setCountry(user.getCountry());

        // Set status
        if (isLoggedIn) {
            this.status = "on";
        }
        else {
            this.status = "off";
        }
    }

    /**
     * @return User Status (logged in = on, otherwise = off)
     */
    public String getStatus(){
        return this.status;
    }

}
