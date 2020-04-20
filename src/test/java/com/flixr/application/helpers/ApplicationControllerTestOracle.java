package com.flixr.application.helpers;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.User;

import java.util.Collections;
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
     * @param expectedMovie
     * @param listOfMovieWithPredictions    Test Output: User Password
     */
    public void validateMoviePredictionsAreHighToLow(String expectedMovie, List<MovieWithPrediction> listOfMovieWithPredictions) {
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
    /* All this is giving issue
    public void validateMoviePredictionsAlphabetical(String expectedMovie, List<MovieWithPrediction> listOfMovieWithPredictions)
    {
        System.out.println("\nValidating recommended movies are in alphabetical order...");
        MovieWithPrediction prevMovie = listOfMovieWithPredictions.get(0); //create variable prevMovie to contain previous movie for comparison

        //for loop iterates over all movies in list
        for(MovieWithPrediction movieWithPrediction : listOfMovieWithPredictions)
        {
            System.out.println("Movie Name: " + movieWithPrediction.getMoviename() + ", " + "Predicted Name: "
                    + prevMovie.getMoviename());

            assertTrue(expectedMovie.equalsIgnoreCase(prevMovie.getMoviename()), "Movies are in alphabetical order"); //compares the movieWithPrediction name with the movie from the prevMovie list
            prevMovie = movieWithPrediction.getMoviename(); //?????? Trying to get the previous movie variable to iterate
        }

    }
*/
    /**
     * Author: Zion Whitehall
     * ZW System Test: test to validate that Add Movie method from test driver ran
     *
     */
    /*I was too frustrated to keep going
    public void validateAddRating(int userID, int imdbID, double rating)
    {
        //dear god what am I even doing

    }
     */

}
