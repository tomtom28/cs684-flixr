// Vraj Desai
package com.flixr.dao;
import com.flixr.beans.User;
import com.flixr.exceptions.DAOException;

import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

public class UserDAO {
//    // Login
//    public void loginUser(String email, String password){
//        try{
//            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
//            PreparedStatement stmt = conn.prepareStatement("SELECT EmailAddress, UserPassword FROM Users");
//            stmt.setString(1,email);
//            stmt.setString(2,password);
//            ResultSet resultSet = stmt.executeQuery();
//            if(resultSet.next()){
//                HttpSession session = request.getSession();
//                session.setAttribute("EmailAddress", email);
//                //response.sendRedirect("Home.jsp");
//            }else{
//                JOptionPane.showMessageDialog(null, "Incorrect Email or Password");
//            }
//            resultSet.close();
//            conn.close();
//        }
//    }
//    //Sign up
//    public void signupUser(String name, String email, String password, int age, String country){
//
//    }


    public User signInUser(String email, String password)  {

        // TODO query DB and populate correct fields
        // SELECT * FROM users WHERE email = ? AND password = ?
        int userId = 1;
        String fullName = "John Smith";
        int age = 19;
        String country = "USA";


        // Create User object
        User user = new User();
        user.setUserID(userId);
        user.setFullname(fullName);
        user.setPassword(password);
        user.setEmail(email);
        user.setAge(age);
        user.SetCountry(country);

        return user;

    }



    public void signUpUser(String email, String fullName, String password, int age, String country) {

        // TODO query database INSERT user table

    }

}
