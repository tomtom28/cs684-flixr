package com.flixr.application;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 */
class RecommendationControllerTest {

    @Nested
    class PredictionEngineTests {

        @Test
        void testGetTopMoviePredictions() {

            // Test UserId 1, top 10
            int userId = 432;
            int numMovies = 10;

            // Test DB Connection
            try {
                // Get top 10 movie predictions
                RecommendationController recommendationController = new RecommendationController();
                List<MovieWithPrediction> predictedMovies = recommendationController.getTopMoviePredictions(userId, numMovies);

                // Check that Movie counts match
                assertEquals(numMovies, predictedMovies.size(), "Number of movies must match the count");

                // TODO not entirely sure how to gauge correctness of the prediction
                // add more tests ...

            } catch (DAOException e) {
                fail("Unable to query database: " + e.getMessage());
            } catch (EngineException e) {
                fail("Unable to generate prediction: " + e.getMessage());
            }
        }

    }


    @Nested
    class RecommendationEngineTests {

        @Test
        void testReTrainingOfRecommendationEngine() {
            try {
                RecommendationController recommendationController = new RecommendationController();
                recommendationController.reTrainModel();
            } catch (EngineException e) {
                fail("Unable to train the Recommendation System: " + e.getEngineMessage());
            }

        }
    }

}

