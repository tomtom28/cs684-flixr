//Vraj Desai
package com.flixr.dao;
import com.flixr.beans.User;
import com.flixr.exceptions.DAOException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

public class UserDAO {
    UserDAO userDAO = new UserDAO();
    User user = new User();
    // Login
    String useremail,userpassword;
    boolean login = false;
    public void loginUser(String email, String password) throws DAOException {
        try{
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT EmailAddress, UserPassword FROM Users");
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
            throw new DAOException(ex);
        }
    }
    //Sign up
    public void signUpUser(String email, String password, String fullname, int age, String country) throws DAOException {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users(EmailAddress,UserPassword,FullName,Age,Country) VALUES (?,?,?,?,?)");
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, fullname);
            stmt.setInt(4, age);
            stmt.setString(5, country);
            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next()) {
                String useremail = resultSet.getString(email);
                String userpassword = resultSet.getString(password);
                String userfullname = resultSet.getString(fullname);
                int userage = resultSet.getInt(age);
                String usercountry = resultSet.getString(country);
            }
        }
        catch(SQLException ex){
            throw new DAOException(ex);
        }
    }
}
