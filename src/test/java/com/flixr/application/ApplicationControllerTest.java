package com.flixr.application;

import com.flixr.Application;
import com.flixr.application.helpers.ApplicationControllerTestDriver;
import com.flixr.application.helpers.ApplicationControllerTestOracle;
import com.flixr.beans.MovieStats;
import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.User;
import com.flixr.exceptions.ApiException;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.flixr.configuration.ApplicationConstantsTest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Green Team
 *
 * Various Tests for the Full Java API for Flixr
 * This is a combined effort for systems tests
 *
 * Please see comments above code snippets for authors of each method/test
 *
 */

public class ApplicationControllerTest {

    private ApplicationControllerTestDriver applicationControllerTestDriver;
    private ApplicationControllerTestOracle applicationControllerTestOracle;

    /**
     * @author Thomas Thompson
     * Fires up the Java API before all unit tests (instance is shared for all tests)
     * Fires up TestDriver and Oracle before EACH test
     */
    @BeforeAll
    public static void initializeAPI() {
        Application.main(new String[]{});
    }
    @BeforeEach
    public void initializeTestSuite() {
        applicationControllerTestDriver = new ApplicationControllerTestDriver();
        applicationControllerTestOracle = new ApplicationControllerTestOracle();
    }


    /**
     * @author Thomas Thompson
     * Test ID: 15
     * Test Type: System
     * Test Name: TT-Api-1
     *
     * Test if the admin user (valid user) can login
     *
     * Criteria:
     * 1) User object should match login credentials
     */
    @Test
    void testValidPasswordForAdminUser() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        try {
            // Login User using test driver and get back User object
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);
            // Check that fields match within the response
            applicationControllerTestOracle.validateUserEmailAndPassword(userEmail, userPassword, user);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * @author Thomas Thompson
     * Test ID: 16
     * Test Type: System
     * Test Name: TT-Api-2
     *
     * Test Top 10 Movies from API
     *
     * Criteria:
     * 1) Number of Movies should be 10
     * 2) Predicted Ratings are sorted in descending order, highest rating to lowest rating
     */
    @Test
    void testTop10MovieRecommendationCounts() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        try {
            // Login User using test driver and get back User object
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Get list of Top 10 movies
            List<MovieWithPrediction> movieWithPredictions = applicationControllerTestDriver.getMovieRecommendations(user.getUserID(), "top10");

            // Check that we get correct number of movie recommendations
            applicationControllerTestOracle.validateMovieCount(10, movieWithPredictions);

            // Check that movies are sorted highest to lowest rating
            applicationControllerTestOracle.validateMoviePredictionsAreHighToLow(movieWithPredictions);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }


    /**
     * @author Thomas Thompson
     * Test ID: 17
     * Test Type: System
     * Test Name: TT-Api-3
     *
     * Test that recommendations will NOT contain any restricted movie for a user under 18
     *
     * Criteria:
     * 1) API will return a list of Top 100 Movie Recommendations
     * 2) None of the movies should have a restricted rated, controlled by application age restrictions
     */
    @Test
    void testUnderAgedUserMovieRecommendations() {

        // Selects known Admin user
        String userEmail = "kid@email.com";
        String userPassword = "password";

        try {
            // Login User using test driver and get back User object
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Get list of Top 100 movies (highest available amount of predictions)
            List<MovieWithPrediction> movieWithPredictions = applicationControllerTestDriver.getMovieRecommendations(user.getUserID(), "top100");

            // Check that we get correct number of movie recommendations
            applicationControllerTestOracle.validateMovieCount(100, movieWithPredictions);

            // Check that movies are NOT age restricted (as defined by ApplicationConstants.RATINGS_NOT_FOR_UNDER_18_YEARS_OLD)
            applicationControllerTestOracle.validateMovieRecommendationsAreAgeRestricted(movieWithPredictions);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }


    /**
     * @author Thomas Thompson
     * Test ID: 18
     * Test Type: System
     * Test Name: TT-Api-4
     *
     * Admin user is able to Re Train the ML model via an API call
     *
     * Criteria:
     * Receive a Success message from the API that ML training has completed in RecommendationEngine
     */
    @Test
    @Ignore // This test is ignored unless it is explicitly called (as it takes a very long time to run)
    void testReTrainingOfRecEngine() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        try {
            // Admin User
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Re Train the RecommendationEngine
            String response = applicationControllerTestDriver.reTrainModel();

            // Check for "Success" message
            boolean isSuccessful = response.contains("Success");
            assertTrue(isSuccessful, "Unable to trigger re-training!");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * @author Thomas Thompson
     * Test ID: 19
     * Test Type: System
     * Test Name: TT-Api-5
     *
     * Admin user is able to receive the MovieStats information, sorted by top average rating
     *
     * Criteria:
     * 1) Admin user is able to query the API for the MovieStats information
     * 2) The MovieStats are sorted by the Top Average Rating (in descending order)
     */
    @Test
    @Ignore // This test is ignored unless it is explicitly called (as it takes a very long time to run)
    void testMovieStatsByAverageRating() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        // Select sort type (ex. "a~z", "z~a", "count", "rating")
        String sortType = "rating";

        try {
            // Admin User Login
            applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Query the API for the MovieStats by the Top Average Rating
            List<MovieStats> listOfMovieStats = applicationControllerTestDriver.getListOfMovieStats(sortType);

            // Verify that the MovieStats are sorted by sortType="rating"
            applicationControllerTestOracle.validateMovieStatsAreSortedCorrectly(listOfMovieStats, sortType);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }


    // TODO ... Keep adding Test Cases here ...
    /**
     * Author: Zion Whitehall
     * ZW System Test: Select the top 25 movies
     */
    @Test
    void testTop25MovieRecommendationCounts() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        try {
            // Login User using test driver and get back User object
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Get list of Top 25 movies
            List<MovieWithPrediction> movieWithPredictions = applicationControllerTestDriver.getMovieRecommendations(user.getUserID(), "top25");

            // Check that we get correct number of movie recommendations
            applicationControllerTestOracle.validateMovieCount(25, movieWithPredictions);

            // Check that movies are sorted highest to lowest rating
            applicationControllerTestOracle.validateMoviePredictionsAreHighToLow(movieWithPredictions);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * Author: Zion Whitehall
     * ZW System Test: Select the top 100 movies
     */
    @Test
    void testTop100MovieRecommendationCounts() {

        // Selects known Admin user
        String userEmail = ADMIN_EMAIL;
        String userPassword = ADMIN_PASSWORD;

        try {
            // Login User using test driver and get back User object
            User user = applicationControllerTestDriver.signInUser(userEmail, userPassword);

            // Get list of Top 10 movies
            List<MovieWithPrediction> movieWithPredictions = applicationControllerTestDriver.getMovieRecommendations(user.getUserID(), "top100");

            // Check that we get correct number of movie recommendations
            applicationControllerTestOracle.validateMovieCount(100, movieWithPredictions);

            // Check that movies are sorted highest to lowest rating
            applicationControllerTestOracle.validateMoviePredictionsAreHighToLow(movieWithPredictions);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * Author: Zion Whitehall
     * System Test: Add user rating via API call
     */
    @Test
    void testPostRating()
    {
        // Selects known Admin user
        String userEmail = "jsmith@email.com";
        String userPassword = "password";


        int imdbID = 13442;
        double rating = 4.0; //true inputs

        User user = applicationControllerTestDriver.signInUser(userEmail, userPassword); //signs in user
        int userID = user.getUserID(); //gets userID from database

        applicationControllerTestDriver.postMovieRating(userID, imdbID, rating); //goes first because driver is input

        applicationControllerTestOracle.validatePostRating(userID, imdbID, rating); //goes last because oracle is output

    }

    /**
     * Author: Zion Whitehall
     * ZW System Test: test to see if movies are stored alphabetically
     */
    @Test
    void testMovieAlphabet() //could say this involves code coverage
    {
        String userEmail = "jsmith@email.com";
        String userPassword = "password";

        User user = applicationControllerTestDriver.signInUser(userEmail, userPassword); //signs in user
        int userID = user.getUserID(); //gets userID from database

        List<MovieWithPrediction> movieWithPredictions = applicationControllerTestDriver.getMovieRecommendations(userID, "a~z"); //called from app controller

        applicationControllerTestOracle.validateMoviePredictionsAlphabetical(movieWithPredictions); //enters in movieswithpredictions list

    }

    /**
     * Author: Zion Whitehall
     * ZW System Test: Test of system will detect incorrect password
     */
    @Test
    void testInvalidPassword() //could say this involves code coverage
    {
        String userEmail = "jsmith@email.com";
        String userPassword = "wrongpassword";


        assertThrows(HttpClientErrorException.class, () ->applicationControllerTestDriver.signInUser(userEmail, userPassword)); //signs in user
        //expected is http error message to be thrown from Api because email and password are incorrect, actual is just test driver hitting Api and getting an exception or a success


    }


}
