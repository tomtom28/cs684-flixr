package com.flixr.dao;

import com.flixr.beans.User;
import com.flixr.beans.UserWithStatus;
import com.flixr.exceptions.DAOException;
import java.sql.*;

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

            // Step 1 - Attempt to Log In User
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

            // Step 2 - Clear any existing login entry (if there is one)
            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM UserSessions WHERE userID = ?");
            stmt2.setInt(1, userId);
            stmt2.executeUpdate();

            // Step 3 - Insert login entry
            PreparedStatement stmt3 = conn.prepareStatement("INSERT INTO UserSessions(UserID) VALUES (?)");
            stmt3.setInt(1, userId);
            stmt3.executeUpdate();

            // Close Connection
            conn.close();

            return user;
        }
        catch(SQLException e)
        {
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
        catch (NullPointerException e) { // Thrown if no entry from DB, i.e. PWD & User did not match
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
    }

    //Sign up
    public User signUpUser(String email, String fullname, String password, int age, String country) throws DAOException {
        try {

            // Step 1 - Insert into DB
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(FullName, UserPassword, EmailAddress, Age, country) VALUES (?,?,?,?,?)");
            // Note that auto increment within the database should be handling the userId
            stmt.setString(1, fullname);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setInt(4, age);
            stmt.setString(5, country);
            stmt.executeUpdate();

            // Step 2 - Query DB to get UserID
            PreparedStatement stmt2 = conn.prepareStatement("SELECT UserID FROM users WHERE EmailAddress = ?");
            stmt2.setString(1, email);
            ResultSet resultSet = stmt2.executeQuery();

            resultSet.next();
            int userId = resultSet.getInt("UserID");

            // Create User
            User user = new User();
            user.setUserID(userId);
            user.setFullname(fullname);
            user.setPassword(password);
            user.setEmail(email);
            user.setAge(age);
            user.setCountry(country);

            // Close & Return
            conn.close();
            return user;

        } catch (SQLException e) {
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
        catch (NullPointerException e) { // Thrown if no entry from DB, i.e. PWD & User did not match
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
    }


    /**
     * @param email User Email Address
     * @return  Returns a UserWithStatus entry
     * @throws DAOException
     */
    public UserWithStatus checkUserStatus(String email) throws DAOException {

        try {
            // Step 1 - Check if email is valid
            User user = new User();
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

            // Step 2 - Check if user is logged in
            PreparedStatement stmt2 = conn.prepareStatement("SELECT COUNT(*) AS SESSION_COUNT FROM UserSessions WHERE userID = ?");
            stmt2.setInt(1, userId);
            ResultSet resultSet2 = stmt2.executeQuery();

            resultSet2.next();
            int numberOfSessionsForUserId = resultSet2.getInt("SESSION_COUNT");

            // Determine Log In status
            boolean isLoggedIn = (numberOfSessionsForUserId > 0);
            UserWithStatus userWithStatus = new UserWithStatus(user, isLoggedIn);

            // Close Connection & Return
            conn.close();
            return userWithStatus;

        } catch (SQLException e) {
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
        catch (NullPointerException e) { // Thrown if invalid user email
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
    }

    /**
     * Added by Thomas Thompson
     * Logs out the User by removing the userID from User Session table
     * @param userId
     */
    public void logOutUser(int userId) throws DAOException {
        try {
            // Remove User Session
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM UserSessions WHERE userID = ?");
            stmt2.setInt(1, userId);
            stmt2.executeUpdate();

            // Close connection
            conn.close();

        } catch (SQLException e) {
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
        catch (NullPointerException e) { // Thrown if invalid user email
            System.out.println("Query Failed: " + e.getMessage());
            throw new DAOException(e);
        }
    }

}
