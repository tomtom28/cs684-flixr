package com.flixr.dao;

import com.flixr.beans.UserSubmission;
import com.flixr.exceptions.DAOException;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.flixr.configuration.ApplicationConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to the EngineDAO
 */
public class EngineDAOTest {

    private static int TEST_USER_ID = 1;

    /**
     * TT: Unit Test # 4
     * Ensure that the "User Submission" (i.e. the movies that a given user rated) are correctly generated for a valid UserId
     */
    @Test
    void testGetUserSubmission() {

        // Test UserId
        int userId = TEST_USER_ID;

        // Test DB Connection
        try {
            // Get UserSubmission from DAO
            EngineDAO engineDAO = new EngineDAO();
            UserSubmission userSubmission = engineDAO.getUserSubmission(userId);

            // Initialize Helper Class
            EngineDAOTestOracle engineDAOTestOracle = new EngineDAOTestOracle();

            // Total Number of rated movies in UserSubmission should match database
            int expectedNumberOfRatings = engineDAOTestOracle.getTotalNumberOfMoviesRatedByUserId(userId);
            assertEquals(expectedNumberOfRatings, userSubmission.getMoviesViewed().size(), "Total Count of Movies Rated should match!");

            // Only Movies rated by the user should appear in the UserSubmission
            List<Integer> expectedListOfAllMovieIdsNotRatedByUserId = engineDAOTestOracle.getListOfAllMovieIdsNotRatedByUserId(userId);
            assertTrue(engineDAOTestOracle.listDoesNotContainAnyMatchingMovieIds(userSubmission, expectedListOfAllMovieIdsNotRatedByUserId),
                    "UserSubmission contained a movieId that was not rated by the user!");

            // Spot Check one of the MovieId Ratings to ensure it matches database
            Random random = new Random();
            int randomMovieIndex = random.nextInt(expectedNumberOfRatings); // from 0 to (# ratings - 1)
            int randomMovieId = userSubmission.getMoviesViewed().get(randomMovieIndex);
            double expectedMovieRating = engineDAOTestOracle.getRatingForMovieId(userId, randomMovieId);
            assertEquals(expectedMovieRating, userSubmission.getMovieRating(randomMovieId),
                    "UserSubmission rating did not match the rating in the database!");

        } catch (SQLException e) {
            fail("Unable to query database: " + e.getMessage());
        } catch (Exception e) {
            fail("Unknown error: " + e.getMessage());
        }
    }


    /**
     * TT: Unit Test # 5
     * Ensure that all "Movies NOT Rated By User" are accurate for a valid UserId
     */
    @Test
    void testGetMovieIdsNotRatedByUserId() {

        // Test UserId
        int userId = TEST_USER_ID;

        try {
            // Get MovieIds not rated by user
            EngineDAO engineDAO = new EngineDAO();
            Collection<Integer> movieIdsNotRatedByUser = engineDAO.getMovieIdsNotRatedByUserId(userId);

            // Only Movies rated by the user should appear in the UserSubmission
            EngineDAOTestOracle engineDAOTestOracle = new EngineDAOTestOracle();
            Collection<Integer> expectedListOfAllMovieIdsNotRatedByUserId = engineDAOTestOracle.getListOfAllMovieIdsNotRatedByUserId(userId);
            if (expectedListOfAllMovieIdsNotRatedByUserId == null) throw new Exception("EngineDAOTestHelper passed null list!");
            assertTrue(movieIdsNotRatedByUser.containsAll(expectedListOfAllMovieIdsNotRatedByUserId),
                    "List of MovieIds NOT Rated By User contained a movieId that WAS rated by User!");

        } catch (DAOException e) {
            fail("EngineDAO Exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unknown error: " + e.getMessage());
        }

    }



    // -----------------------------------------------------------------------------------------------------------------


    // Helper Methods for EngineDAOTest
    private class EngineDAOTestOracle {

        /**
         * @param userId    User Id
         * @return  Total Count of movies rated by this user
         */
        private int getTotalNumberOfMoviesRatedByUserId(int userId) {
            try {
                Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS RatingCount FROM ratings WHERE userId = ?");
                stmt.setInt(1, userId);
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
                int ratingCount = resultSet.getInt("RatingCount");

                // Close connection and return
                conn.close();
                return ratingCount;

            } catch (SQLException e) {
                System.out.println("Error in EngineDAOTestHelper! Possible database connection issue");
                e.printStackTrace();
                return -1;
            }
        }


        /**
         * @param userId    User Id
         * @return  List of all Movies NOT Rated By this user
         */
        private List<Integer> getListOfAllMovieIdsNotRatedByUserId(int userId) {
            try {
                Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                String query =  "SELECT distinct imdbId FROM flixr.ratings " +
                                "WHERE imdbId NOT IN ( " +
                                    "SELECT imdbId FROM flixr.ratings where userId = ? " +
                                ")";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet resultSet = stmt.executeQuery();

                // Iterate over ResultSet to create UserSubmission for a given UserId
                List<Integer> listOfAllMovieIdsNotRatedByUserId = new ArrayList<>();
                while (resultSet.next()) {
                    int movieId = resultSet.getInt("imdbId");
                    listOfAllMovieIdsNotRatedByUserId.add(movieId);
                }

                // Close connection and return
                conn.close();
                return listOfAllMovieIdsNotRatedByUserId;

            } catch (SQLException e) {
                System.out.println("Error in EngineDAOTestHelper! Possible database connection issue");
                e.printStackTrace();
                return null;
            }
        }

        /**
         * @param userSubmission    UserSubmission entry (to be tested)
         * @param listOfAllMovieIdsNotRatedByUserId List Of All Movies NOT rated by the user (to be validated against)
         * @return  Returns "true" if the UserSubmission only had movies rated by the user
         */
        private boolean listDoesNotContainAnyMatchingMovieIds(UserSubmission userSubmission, List<Integer> listOfAllMovieIdsNotRatedByUserId) {
            for (int movieId : userSubmission.getMoviesViewed()) {
                // Check if UserSubmission has a movie that was NOT rated by the userId
                if(listOfAllMovieIdsNotRatedByUserId.contains(movieId)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @param userId    User Id
         * @param movieId   Movie Id
         * @return  Rating for the given (UserId, MovieId) key
         */
        private double getRatingForMovieId(int userId, int movieId) {
            try {
                Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement("SELECT rating FROM ratings WHERE userId = ? AND imdbId = ?");
                stmt.setInt(1, userId);
                stmt.setInt(2, movieId);
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
                double rating = resultSet.getDouble("rating");

                // Close connection and return
                conn.close();
                return rating;

            } catch (SQLException e) {
                System.out.println("Error in EngineDAOTestHelper! Possible database connection issue");
                e.printStackTrace();
                return -1.0;
            }
        }

    }

}
