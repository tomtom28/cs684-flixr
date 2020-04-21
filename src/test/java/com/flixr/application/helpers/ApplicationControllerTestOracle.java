package com.flixr.application.helpers;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.User;

import java.sql.*;
import java.util.Collections;
import java.util.List;

import static com.flixr.configuration.ApplicationConstants.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Green Team
 *
 * Test Oracle for ApplicationControllerTests
 *
 */
public class ApplicationControllerTestOracle {

    /**
     * @author Thomas Thompson
     * Validates whether user object matches the input email and password
     *
     * @param userEmail     Known Input: User Email Address
     * @param userPassword  Known Input: User Password
     * @param user          Test Output: User object returned from API
     */
    public void validateUserEmailAndPassword(String userEmail, String userPassword, User user) {
        System.out.println("Validating user: " + userEmail + "...");
        assertEquals(userEmail, user.getEmail(), "User Email returned from API does not match!");
        assertEquals(userPassword, user.getPassword(), "User Password returned from API does not match!");
    }


    /**
     * @author Thomas Thompson
     * Validates whether number of movies matches the expected count
     *
     * @param expectedCount                 Known Input: Expected # of Movies
     * @param listOfMovieWithPredictions    Test Output: User Password
     */
    public void validateMovieCount(int expectedCount, List<MovieWithPrediction> listOfMovieWithPredictions) {
        System.out.println("\nValidating recommended movie count...");
        System.out.println("Number Of Predicted Movies: " + listOfMovieWithPredictions.size());
        assertEquals(expectedCount, listOfMovieWithPredictions.size(), "Number of items in the movie predictions does not match expected count!");
    }


    /**
     * @author Thomas Thompson
     * Validates whether movies are sorted highest to lowest order
     *
     * @param listOfMovieWithPredictions    Test Output: User Password
     */
    public void validateMoviePredictionsAreHighToLow(List<MovieWithPrediction> listOfMovieWithPredictions) {
        System.out.println("\nValidating recommended movies are in highest to lowest predicted rating order...");

        // Iterate over all movies in the list
        double prevPredictionRating = Double.MAX_VALUE;
        for (MovieWithPrediction movieWithPrediction : listOfMovieWithPredictions) {
            System.out.println("Movie Name: " + movieWithPrediction.getMoviename() + ", " + "Predicted Rating: " + movieWithPrediction.getPredictedRating());
            assertTrue(movieWithPrediction.getPredictedRating() <= prevPredictionRating, "Predicted Movie Rating is out of order!");
            prevPredictionRating = movieWithPrediction.getPredictedRating(); // update "previous" movie rating prediction for next iteration
        }

    }




    // TODO - Keep adding your validation methods here ...

    /**
     * Author: Zion Whitehall
     * ZW System Test: test to see if movies are stored alphabetically
     *
     * @param expectedMovie
     * @param listOfMovieWithPredictions
     */
    public void validateMoviePredictionsAlphabetical( List<MovieWithPrediction> listOfMovieWithPredictions)
    {
        System.out.println("\nValidating recommended movies are in alphabetical order...");
        String prevMovieName = listOfMovieWithPredictions.get(0).getMoviename(); //create variable prevMovie to contain previous movie for comparison

        //for loop iterates over all movies in list
        for(MovieWithPrediction movieWithPrediction : listOfMovieWithPredictions)
        {
            System.out.println("Movie Name: " + movieWithPrediction.getMoviename() + ", " + "Predicted Name: "
                    + prevMovieName);

            assertTrue(movieWithPrediction.getMoviename().compareTo(prevMovieName) >= 0, "Movies are not in alphabetical order"); //compares the movieWithPrediction name with the movie from the prevMovie list, if compareto != 0 then something is wrong
            prevMovieName = movieWithPrediction.getMoviename(); //update previous name
        }

    }

    /**
     * Author: Zion Whitehall
     * ZW System Test: test to validate that post Movie method from test driver ran
     *
     */

    public void validatePostRating(int userID, int imdbID, double rating)
    {
        //check that movie and rating is in database
        //if exist, pass

        //taken from ratingDAO
        try
        {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ratings WHERE userId = ? AND imdbId = ?"); //? because it needs to pass variable
            stmt.setInt(1, userID);
            stmt.setInt(2, imdbID);
            //See if rating was done correctly

            ResultSet rs = stmt.executeQuery();
            rs.next();
            double dbRating = rs.getDouble("rating"); //gets rating from database

            assertEquals(rating, dbRating, "db value and supplied value did not match"); //tests the getRating and the rating
            //if it fails then the ratings are different

            // Close connection
            String insertedTuple = "(" + userID + "," + imdbID + "," + rating + ")";
            System.out.println("Select Query Completed: " + insertedTuple);
            conn.close();
        }
        catch (SQLException e)
        {
            fail("Select Query Failed!");
        }
    }



}
