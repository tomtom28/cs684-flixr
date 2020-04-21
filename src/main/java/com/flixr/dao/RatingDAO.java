package com.flixr.dao;

/**
 * Authors: Zion Whitehall, Thomas Thompson
 * Inserts new Ratings by Users
 * Returns Movie Analytics to Admin Page
 */

import com.flixr.beans.MovieStats;
import com.flixr.exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.flixr.configuration.ApplicationConstants.*;

public class RatingDAO
{
    // Thomas
    public void addMovieRating(int userID, int imdbID, double rating) {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt0 = conn.prepareStatement("DELETE FROM ratings WHERE userID = ? AND imdbID = ?"); //delete if user already entered it
            stmt0.setInt(1, userID);
            stmt0.setInt(2, imdbID);
            // Insert all matrix row entries in 1 batch
            stmt0.executeUpdate();

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ratings(userId, imdbId, rating) VALUES (?, ?, ?)");
            stmt.setInt(1, userID);
            stmt.setInt(2, imdbID);
            stmt.setDouble(3, rating);

            // Insert all matrix row entries in 1 batch
            stmt.executeUpdate();

            // Close connection
            String insertedTuple = "(" + userID + "," + imdbID + "," + rating + ")";
            System.out.println("Insert Query Completed: " + insertedTuple);
            conn.close();

        } catch (SQLException e) {
            System.out.println("Insert Query Failed!");
        }

    }

    // Zion
    public List<MovieStats> getMovieStatsByAvgRating() throws DAOException
    {
        try
        {
            String query = "SELECT * FROM MovieStats ORDER BY AverageRating DESC LIMIT " + MAX_ADMIN_LIMIT;
            //query database for list of movies ordered by MovieName and the AVG and COUNT of ratings
            //Connect to the SQL server
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            List<MovieStats> allMovieStats = new ArrayList<>();
            ResultSet resultSet = stmt.executeQuery();
            // Iterate Over results & add to list
            while(resultSet.next())
            {
                int imdbId = resultSet.getInt("MovieId");
                String movieTitle = resultSet.getString("MovieName");
                int totalRatingCount = resultSet.getInt("RatingCount");
                double averageRating = resultSet.getDouble("AverageRating");
                MovieStats movieStats = new MovieStats(imdbId, movieTitle, totalRatingCount, averageRating);
                allMovieStats.add(movieStats);
            }

            return allMovieStats;
        }
        catch (SQLException e)
        {
            throw new DAOException(e);
        }

    }

    // Zion
    public List<MovieStats> getMovieStatsByTotalCount() throws DAOException
    {

        List<MovieStats> allMovieStats = new ArrayList<>();

        try
        {
            String query = "SELECT * FROM MovieStats ORDER BY RatingCount DESC LIMIT " + MAX_ADMIN_LIMIT;
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            // Iterate Over results & add to list
            while(resultSet.next())
            {
                int imdbId = resultSet.getInt("MovieId");
                String movieTitle = resultSet.getString("MovieName");
                int totalRatingCount = resultSet.getInt("RatingCount");
                double averageRating = resultSet.getDouble("AverageRating");
                MovieStats movieStats = new MovieStats(imdbId, movieTitle, totalRatingCount, averageRating);
                allMovieStats.add(movieStats);
            }

            return allMovieStats;
        }
        catch (SQLException e)
        {
            throw new DAOException(e);
        }

    }

    // Zion
    public List<MovieStats> getMovieStatsByAtoZ() throws DAOException{

        List<MovieStats> allMovieStats = new ArrayList<>();

        try
        {
            String query = "SELECT * FROM MovieStats ORDER BY MovieName ASC LIMIT " + MAX_ADMIN_LIMIT;//select query goes here
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            // Iterate Over results & add to list
            while (resultSet.next()) {
                int imdbId = resultSet.getInt("MovieId");
                String movieTitle = resultSet.getString("MovieName");
                int totalRatingCount = resultSet.getInt("RatingCount");
                double averageRating = resultSet.getDouble("AverageRating");
                MovieStats movieStats = new MovieStats(imdbId, movieTitle, totalRatingCount, averageRating);
                allMovieStats.add(movieStats);
            }

            return allMovieStats;
        }
        catch (SQLException e)
        {
            throw new DAOException(e);
        }

    }

    public List<MovieStats> getMovieStatsByZtoA() throws DAOException
    {

        List<MovieStats> allMovieStats = new ArrayList<>();

        try {
            String query = "SELECT * FROM MovieStats ORDER BY MovieName DESC LIMIT " + MAX_ADMIN_LIMIT;//select query goes here
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            // Iterate Over results & add to list
            while (resultSet.next()) {
                int imdbId = resultSet.getInt("MovieId");
                String movieTitle = resultSet.getString("MovieName");
                int totalRatingCount = resultSet.getInt("RatingCount");
                double averageRating = resultSet.getDouble("AverageRating");
                MovieStats movieStats = new MovieStats(imdbId, movieTitle, totalRatingCount, averageRating);
                allMovieStats.add(movieStats);
            }

            return allMovieStats;
        }
        catch (SQLException e)
        {
            throw new DAOException(e);
        }

    }



}
