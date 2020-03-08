package com.flixr.dao;

import com.flixr.beans.UserSubmission;
import com.flixr.interfaces.IPredictionEngineDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static com.flixr.configuration.ApplicationConstants.*;

/**
 * @author Thomas Thompson
 *
 * Used to query the database to create Recommendation Engine user submission inputs & save the trained model to the database
 * Also used to by the Prediction Engine to create user submission inputs & query the trained model
 *
 */
public class EngineDAO implements IPredictionEngineDAO {

    public EngineDAO() {}


    // TODO TOM FINISH THIS ONCE MY MODEL IS UPLOADED TO DB
    /**
     * Gets the Correlation between Movies in the trained Recommendation Model
     * @param movieId_i     MovieId in Matrix position i
     * @param movieId_j     MovieId in Matrix position j
     * @return  Correlation (i.e. average preference difference between Movie i and Movie j)
     */
    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) {
        return 1.0;
    }


    /**
     * @param userId    User Id
     * @return Returns all movieIds that were NOT rated by a given UserId
     * @throws SQLException
     */
    public Collection<Integer> getMovieIdsNotRatedByUserId(int userId) throws SQLException {

        // Query DB for all MovieIds not rated by given user
        Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
        String query =  "SELECT DISTINCT imdbId " +
                        "FROM ratings " +
                        "WHERE imdbId NOT IN ( " +
                            "SELECT imdbId FROM ratings WHERE userId = ? " +
                        ")";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        ResultSet resultSet = stmt.executeQuery();

        // Iterate over ResultSet to create list of MovieIds
        Collection<Integer> movieIdsNotRatedByUserId = new ArrayList<>();
        while (resultSet.next()) {
            int movieId = resultSet.getInt("imdbId");
            movieIdsNotRatedByUserId.add(movieId);
        }

        return movieIdsNotRatedByUserId;
    }



    /**
     * @param   userId  User Id
     * @return  UserSubmission for a given User Id with all the Movies the user Rated
     * @throws  SQLException
     */
    public UserSubmission getUserSubmission(int userId) throws SQLException {

        // Query DB for all Ratings for given user
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

        return userSubmission;
    }

}
