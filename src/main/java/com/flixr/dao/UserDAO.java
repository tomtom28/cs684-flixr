//Vraj Desai
package com.flixr.dao;
import com.flixr.beans.User;
import com.flixr.exceptions.DAOException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

public class UserDAO {
    // Login
    String useremail,userpassword;
    boolean login = false;
    public User signInUser(String email, String password){
        try{
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE email = ? and password = ?");
            stmt.setString(1,email);
            stmt.setString(2,password);
            ResultSet resultSet = stmt.executeQuery();

            while(resultSet.next()){
                useremail = resultSet.getString(email);
                userpassword = resultSet.getString(password);
                if(useremail.equals(email) && userpassword.equals(password)){
                    login = true;
                }
            }
            resultSet.close();
            conn.close();
        }
        catch(SQLException ex)
        {
            System.out.println("Query Failed");
        }
        
        int userId = 1;
        String fullName = "John Smith";
        int age = 19;
        String country = "USA";

        User user = new User();
        user.setUserID(userId);
        user.setFullname(fullName);
        user.setPassword(password);
        user.setEmail(email);
        user.setAge(age);
        user.setCountry(country);
        return user;
    }

    //Sign up
    public void signUpUser(String email, String password, String fullname, int age, String country){
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users(EmailAddress,UserPassword,FullName,Age,Country) VALUES (?,?,?,?,?)");
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, fullname);
            stmt.setInt(4, age);
            stmt.setString(5, country);
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Query Failed");
        }
    }
}
