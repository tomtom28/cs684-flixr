package com.flixr.application;

/**
 * Author: Zion Whitehall
 * Tests the connection to the flixr database
 */

import org.junit.jupiter.api.Test;
import org.testng.annotations.AfterTest;

import java.sql.*;

import static com.flixr.configuration.ApplicationConstants.*;
import static org.junit.jupiter.api.Assertions.fail;

public class DatabaseTest {
    @Test
    void testMoviesTable() {
        try {
            String query = "SELECT * FROM movies"; //queries the database in order to select from movies table

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to movies table.");
        }

    }

    @Test
    void testLinksTable() {
        try {
            String query = "SELECT * FROM links"; //queries the database in order to select from movies table

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to links table.");
        }
    }

    @Test
    void testGenreTable() {
        try {
            String query = "SELECT * FROM genre";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to genre table.");
        }
    }

    @Test
    void testAwardsTable() {
        try {
            String query = "SELECT * FROM awards";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to awards table.");
        }
    }

    @Test
    void testRatingsTable() {
        try {
            String query = "SELECT * FROM ratings";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to ratings table.");
        }
    }

    @Test
    void testRecEngTable() {
        try {
            String query = "SELECT * FROM recenginemodel";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to recengine table.");
        }
    }

    @Test
    void testTagsTable() {
        try {
            String query = "SELECT * FROM tags";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to tags table.");
        }
    }

    @Test
    void testUsersTable() {
        try {
            String query = "SELECT * FROM users";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to users table.");
        }
    }

}
