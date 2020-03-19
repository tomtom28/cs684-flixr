//Vraj Desai
package com.flixr.dao;
import com.flixr.exceptions.DAOException;
import java.sql.*;
import java.util.*;

public class UserDAO {
    // Login
    public void loginUser(String email, String password){
        try{
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT EmailAddress, UserPassword FROM Users");
            stmt.setString(1,email);
            stmt.setString(2,password);
            ResultSet resultSet = stmt.executeQuery();
            if(resultSet.next()){
                HttpSession session = request.getSession();
                session.setAttribute("EmailAddress", email);
                //response.sendRedirect("Home.jsp");
            }else{
                JOptionPane.showMessageDialog(null, "Incorrect Email or Password");
            }
            resultSet.close();
            conn.close();
        }
    }
    //Sign up
    public void signupUser(String name, String email, String password, int age, String country){

    }
}
