package com.flixr.application;

import com.flixr.beans.PredictedMovie;
import com.flixr.beans.UserSubmission;
import com.flixr.dao.EngineDAO;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationControllerTest {

    @Test
    void testGetTopMoviePredictions() {

        // Test UserId 1, top 10
        int userId = 1;
        int numMovies = 10;

        // Test DB Connection
        try {
            // Get top 10 movie predictions
            RecommendationController recommendationController = new RecommendationController();
            List<PredictedMovie> predictedMovies = recommendationController.getTopMoviePredictions(userId, numMovies);

            // Check that Movie counts match
            assertEquals(numMovies, predictedMovies.size(), "Number of movies must match the count");

            // TODO not entirely sure how to gauge correctness of the prediction
            // add more tests ...

        } catch (SQLException e) {
            fail("Unable to query database: " + e.getMessage());
        }

    }

}