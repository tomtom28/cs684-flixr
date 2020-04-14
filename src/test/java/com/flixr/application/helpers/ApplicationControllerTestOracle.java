package com.flixr.application.helpers;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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



}
