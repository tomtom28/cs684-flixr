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
 * Integration Tests related to the Recommendatation Controller
 */
class RecommendationControllerTest {

    @Nested
    class PredictionEngineTests {

        /**
         * @author Thomas Thompson
         * Test ID: 6
         * Test Type: Integration
         * Test Name: RecommendationControllerTest-1
         *
         * Retrieve N number of MoviesWithPredictions, ensure that DB and Engine interfaces are working
         * Note: that it is assumed that the unit tests for PredictionDAO and PredictionEngine are judging the accuracy of the predictions
         */
        @Test
        void testGetTopMoviePredictions() {

            // Test UserId 1, top 10
            int userId = 1;
            int numMovies = 10;

            // Test DB Connection
            try {
                // Get top 10 movie predictions
                RecommendationController recommendationController = new RecommendationController();
                List<MovieWithPrediction> predictedMovies = recommendationController.getTopMoviePredictions(userId, numMovies);

                // Movie count must match
                assertEquals(numMovies, predictedMovies.size(), "Number of movies must match the count");

            } catch (DAOException e) {
                fail("Unable to query database: " + e.getMessage());
            } catch (EngineException e) {
                fail("Unable to generate prediction: " + e.getMessage());
            } catch (Exception e) {
                fail("Unknown error: " + e.getMessage());
            }
        }

    }


    @Nested
    class RecommendationEngineTests {

        /**
         * @author Thomas Thompson
         * Test ID: 7
         * Test Type: Integration
         * Test Name: RecommendationControllerTest-2
         *
         * Retrain the model, ensure that Engine & I/O interfaces are working
         * Note: that it is assumed that the unit tests for RecommendationEngine will assess performance and/or accuracy
         */
        @Test
        void testReTrainingOfRecommendationEngine() {
            try {
                RecommendationController recommendationController = new RecommendationController();
                recommendationController.reTrainModel();
            } catch (EngineException e) {
                fail("Unable to train the Recommendation Engine: " + e.getEngineMessage());
            } catch (Exception e) {
                fail("Unable to train the Recommendation Engine: " + e.getMessage());
            }

        }
    }

}

