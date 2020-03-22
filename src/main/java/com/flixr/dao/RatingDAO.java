package com.flixr.dao;

import com.flixr.beans.MovieStats;
import com.flixr.exceptions.DAOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.flixr.configuration.ApplicationConstants.*;

public class RatingDAO
{
    public void addMovieRating(int userID, int imdbID, double rating) {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ratings(userId, imdbId, rating) VALUES (?, ?, ?)");
            stmt.setInt(1, userID);
            stmt.setInt(2, imdbID);
            stmt.setDouble(3, rating);

            // Insert all matrix row entries in 1 batch
            stmt.executeUpdate();

            // Close connection
            conn.close();

        } catch (SQLException e) {
            System.out.println("Insert Query Failed!");
        }

    }

    public List<MovieStats> getMovieStatsByAvgRating() {

        // TODO - ZION PLEASE ADD THE SQL JOINS HERE (see our Slack conversations from the other week)
        // SELECT QUERY to get all movies (like in MovieDAO) but then also added DB stats
        // Might neeed a SQL GROUP BY imdbId query
        // use AVG(rating) and Count(Rating)
        // ORDER BY MovieTitle (or however its called in the DB)

        List<MovieStats> allMovieStats = new ArrayList<>();

        // TODO - ZION iterate over ResultSet here
        // Iterate Over results & add to list
        int imdbId = 100;
        String movieTitle = "Test Movie";
        int totalRatingCount = 1000;
        double averageRating = 4.4;
        MovieStats movieStats = new MovieStats(imdbId,movieTitle,totalRatingCount,averageRating);
        allMovieStats.add(movieStats);

        return allMovieStats;

    }

    public List<MovieStats> getMovieStatsByTotalCount() {

        // TODO - ZION PLEASE ADD THE SQL JOINS HERE (see our Slack conversations from the other week)
        // SELECT QUERY .... see slack

        List<MovieStats> allMovieStats = new ArrayList<>();

        // TODO - ZION iterate over ResultSet here
        // Iterate Over results & add to list
        int imdbId = 100;
        String movieTitle = "Test Movie";
        int totalRatingCount = 1000;
        double averageRating = 4.4;
        MovieStats movieStats = new MovieStats(imdbId,movieTitle,totalRatingCount,averageRating);
        allMovieStats.add(movieStats);

        return allMovieStats;

    }

    public List<MovieStats> getMovieStatsByAtoZ() {

        // TODO - ZION PLEASE ADD THE SQL JOINS HERE (see our Slack conversations from the other week)
        // SELECT QUERY .... see slack

        List<MovieStats> allMovieStats = new ArrayList<>();

        // TODO - ZION iterate over ResultSet here
        // Iterate Over results & add to list
        int imdbId = 100;
        String movieTitle = "Test Movie";
        int totalRatingCount = 1000;
        double averageRating = 4.4;
        MovieStats movieStats = new MovieStats(imdbId,movieTitle,totalRatingCount,averageRating);
        allMovieStats.add(movieStats);

        return allMovieStats;

    }

    public List<MovieStats> getMovieStatsByZtoA() {

        // TODO - ZION PLEASE ADD THE SQL JOINS HERE (see our Slack conversations from the other week)
        // SELECT QUERY .... see slack

        List<MovieStats> allMovieStats = new ArrayList<>();

        // TODO - ZION iterate over ResultSet here
        // Iterate Over results & add to list
        int imdbId = 100;
        String movieTitle = "Test Movie";
        int totalRatingCount = 1000;
        double averageRating = 4.4;
        MovieStats movieStats = new MovieStats(imdbId,movieTitle,totalRatingCount,averageRating);
        allMovieStats.add(movieStats);

        return allMovieStats;

    }



}
