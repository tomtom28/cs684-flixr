package com.flixr.dao;

import com.flixr.beans.MovieStats;
import com.flixr.exceptions.DAOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

//    public List<MovieStats> getMovieStatsByAvgRating()
//    {
//        // SELECT QUERY to get all movies (like in MovieDAO) but then also added DB stats
//        // Might neeed a SQL GROUP BY imdbId query
//        // use AVG(rating) and Count(Rating)
//        // ORDER BY MovieTitle (or however its called in the DB)
//    }
}
