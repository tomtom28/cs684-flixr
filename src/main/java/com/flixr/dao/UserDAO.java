package com.flixr.dao;

import com.flixr.beans.User;
import com.flixr.exceptions.DAOException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

/**
 * @author Vraj Desai
 * With edits by Thomas Thompson
 */

public class UserDAO {
    // Login
    public User signInUser(String email, String password) throws DAOException {
        User user = new User();
        try{
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE EmailAddress = ? and UserPassword = ?");
            stmt.setString(1,email);
            stmt.setString(2,password);
            ResultSet resultSet = stmt.executeQuery();

            resultSet.next();
            int userId = resultSet.getInt("UserId");
            String fullName = resultSet.getString("FullName");
            String userEmail = resultSet.getString("EmailAddress");
            String userPassword = resultSet.getString("UserPassword");
            String country = resultSet.getString("Country");
            int age = resultSet.getInt("Age");

            user.setUserID(userId);
            user.setFullname(fullName);
            user.setPassword(password);
            user.setEmail(email);
            user.setAge(age);
            user.setCountry(country);

            conn.close();
        }
        catch(SQLException | NullPointerException ex)
        {
            System.out.println("Query Failed: " + ex.getMessage());
        }
        return user;
    }

    //Sign up
    public void signUpUser(String email, String fullname, String password, int age, String country) {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("");
            stmt.setString(1, fullname);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setInt(4, age);
            stmt.setString(5, country);
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Query Failed");
        }
    }


    // hacky fix for checkStatus
    public User checkUserStatus(String email) {
        User user = new User();
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE EmailAddress = ?");
            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();

            resultSet.next();
            int userId = resultSet.getInt("UserId");
            String fullName = resultSet.getString("FullName");
            String userEmail = resultSet.getString("EmailAddress");
            String userPassword = resultSet.getString("UserPassword");
            String country = resultSet.getString("Country");
            int age = resultSet.getInt("Age");

            user.setUserID(userId);
            user.setFullname(fullName);
            user.setPassword(userPassword);
            user.setEmail(userEmail);
            user.setAge(age);
            user.setCountry(country);

            conn.close();
        } catch (SQLException e) {
            System.out.println("Query Failed");
        }
        return user;
    }

}
