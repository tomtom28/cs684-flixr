package com.flixr.dao;

import com.flixr.beans.UserSubmission;
import com.flixr.exceptions.DAOException;
import com.flixr.interfaces.IPredictionEngineDAO;

import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

/**
 * @author Thomas Thompson
 *
 * Used to query the database to create Recommendation Engine user submission inputs & save the trained model to the database
 * Also used to by the Prediction Engine to create user submission inputs & query the trained model
 *
 */
public class EngineDAO {

    public EngineDAO() {}

    /**
     * @return  Returns a list of (sorted) unique MovieIds
     * @throws DAOException
     */
    public TreeSet<Integer> getDistinctMovieIds() throws DAOException {
        TreeSet<Integer> distinctMovieIds = new TreeSet<>();
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT imdbId FROM ratings ORDER BY imdbId");
            ResultSet resultSet = stmt.executeQuery();

            // Iterate Over MovieIds
            while (resultSet.next()) {
                int movieId = resultSet.getInt("imdbId");
                distinctMovieIds.add(movieId);
            }
            conn.close();
            return distinctMovieIds;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    /**
     * @param userId    User Id
     * @return Returns all movieIds that were NOT rated by a given UserId
     * @throws SQLException
     */
    public Collection<Integer> getMovieIdsNotRatedByUserId(int userId) throws DAOException {

        // Query DB for all MovieIds not rated by given user
        try {
            String query = "SELECT DISTINCT imdbId " +
                    "FROM ratings " +
                    "WHERE imdbId NOT IN ( " +
                    "SELECT imdbId FROM ratings WHERE userId = ? " +
                    ")";
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            // Iterate over ResultSet to create list of MovieIds
            Collection<Integer> movieIdsNotRatedByUserId = new ArrayList<>();
            while (resultSet.next()) {
                int movieId = resultSet.getInt("imdbId");
                movieIdsNotRatedByUserId.add(movieId);
            }

            // Close connection and return
            conn.close();
            return movieIdsNotRatedByUserId;

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }


    /**
     * @param   userId  User Id
     * @return  UserSubmission for a given User Id with all the Movies the user Rated
     * @throws  SQLException
     */
    public UserSubmission getUserSubmission(int userId) throws DAOException {

        // Query DB for all Ratings for given user
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT imdbId, rating FROM ratings WHERE userId = ?");
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            // Iterate over ResultSet to create UserSubmission for a given UserId
            UserSubmission userSubmission = new UserSubmission(userId);
            while (resultSet.next()) {
                int movieId = resultSet.getInt("imdbId");
                double rating = resultSet.getDouble("rating");
                userSubmission.addMovieRating(movieId, rating);
            }

            // Close connection and return
            conn.close();
            return userSubmission;

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }


    /**
     * @return  Returns a list of all UserSubmissions in the Ratings table
     * @throws DAOException
     */
    public TreeMap<Integer, UserSubmission> getAllUserSubmissions() throws DAOException {
        try {
            // Initialize user submissions
            TreeMap<Integer, UserSubmission> userSubmissions = new TreeMap<>();

            // Query Ratings table for all UserSubmissions
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT userId FROM ratings");
            ResultSet resultSet = stmt.executeQuery();

            // Iterate over all User Id's in the ResultSet
            while (resultSet.next()) {
                int userId = resultSet.getInt("userId");
                UserSubmission userSubmission = this.getUserSubmission(userId);
                userSubmissions.put(userId, userSubmission);
            }

            // Close connection and return
            conn.close();
            return userSubmissions;

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }



    public void saveMatrixRowToDB(List<Number[]> matrixRow) throws DAOException {

        // Generate Query for current Matrix Row
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO RecEngineModel VALUES (?,?,?)");

            // Iterate over all indexes in matrix row
            for (Number[] matrixIndex : matrixRow) {
                int movieId_i = (int) matrixIndex[0];
                int movieId_j = (int) matrixIndex[1];
                double avgDifference = (double) matrixIndex[2];
                stmt.setInt(1, movieId_i);
                stmt.setInt(2, movieId_j);
                stmt.setDouble(3, avgDifference);
                stmt.addBatch();
            }

            // Insert all matrix row entries in 1 batch
            stmt.executeBatch();

            // Close connection
            conn.close();

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

}
