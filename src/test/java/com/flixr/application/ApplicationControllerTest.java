package com.flixr.application;

import com.flixr.Application;
import com.flixr.application.helpers.ApplicationControllerTestDriver;
import com.flixr.application.helpers.ApplicationControllerTestOracle;
import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.flixr.configuration.ApplicationConstantsTest.*;
import static org.junit.jupiter.api.Assertions.fail;

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
     * TT System Test # 1
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
     * TT System Test # 2
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


    // TODO ... Keep adding Test Cases here ...


}
