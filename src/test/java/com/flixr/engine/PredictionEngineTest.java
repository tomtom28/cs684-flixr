package com.flixr.engine;

import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;
import com.flixr.dao.EngineDAO;
import com.flixr.dao.PredictionDAO;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.exceptions.TestException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to the PredictionEngine
 */
class PredictionEngineTest {

    private static int TEST_USER_ID = 1;
    private static int NUM_OF_MOVIE_PREDICIONS = 10;

    /**
     * TT: Integration Test # 1
     * This test is meant to ensure that the PredictionEngine and its DAOs are properly aligned
     */
    @Test
    void testGetTopXMoviePredictions() {

        int userId = TEST_USER_ID;
        int numberofPredictions = NUM_OF_MOVIE_PREDICIONS;

        try {
            // Initialize Variables needed for Prediction Engine
            PredictionEngineOracle predictionEngineOracle = new PredictionEngineOracle();
            EngineDAO engineDAO = new EngineDAO();
            Collection<Integer> listOfMovieIdsNotRatedByUser = engineDAO.getMovieIdsNotRatedByUserId(userId);
            UserSubmission userSubmission = engineDAO.getUserSubmission(userId); // Full User Submission (i.e. everything they rated)

            // Initialize Prediction Engine & generate predictions
            PredictionDAO predictionDAO = new PredictionDAO();
            PredictionEngine predictionEngine = new PredictionEngine(userSubmission, listOfMovieIdsNotRatedByUser, predictionDAO);
            predictionEngine.generatePredictions();
            List<Prediction> predictions = predictionEngine.getTopXMoviePredictions(numberofPredictions);

            // Movie count must match
            assertEquals(numberofPredictions, predictions.size(), "Number of predictions must match requested count!");

            // Predictions should NOT be movies that the user already rated
            assertTrue( predictionEngineOracle.doesNotContainAnyAlreadyRatedMovies(predictions, userSubmission),
                    "Movie Predictions cannot contain Movies that were already rated by the user!");

            // Predictions must be sorted in descending order
            assertTrue(predictionEngineOracle.hasMovieRatingsInDescOrder(predictions), "Movie Predictions must be sorted, highest to lowest!");

        } catch (DAOException e) {
            fail("DAO Exception was thrown: " + e.getMessage());
        } catch (EngineException e) {
            fail("Engine Exception was thrown: " + e.getMessage());
        }


    }


    /**
     * TT: Unit Test # 7
     * This test is meant to ensure the accuracy of the PredictionEngine results
     */
    @Test
    void testAccuracyOfTopXMoviePredictions() {

        int userId = TEST_USER_ID;
        int numberofPredictions = NUM_OF_MOVIE_PREDICIONS;

        try {
            // Initialize Variables needed for Prediction Engine
            PredictionEngineOracle predictionEngineOracle = new PredictionEngineOracle();
            EngineDAO engineDAO = new EngineDAO();
            Collection<Integer> listOfMovieIdsNotRatedByUser = engineDAO.getMovieIdsNotRatedByUserId(userId);
            UserSubmission fullUserSubmission = engineDAO.getUserSubmission(userId); // Full User Submission (i.e. everything they rated)
            UserSubmission halfUserSubmission = predictionEngineOracle.getHalfUserSubmission(fullUserSubmission); // Half User Submission (i.e. half of what they rated)

            // Initialize Prediction Engine & generate predictions
            PredictionDAO predictionDAO = new PredictionDAO();
            PredictionEngine predictionEngine = new PredictionEngine(fullUserSubmission, listOfMovieIdsNotRatedByUser, predictionDAO);
            predictionEngine.generatePredictions();
            List<Prediction> predictions = predictionEngine.getTopXMoviePredictions(numberofPredictions);

            // TODO: gauge accuracy of the prediction
            // compare fullUserSubmission with halfUserSubmission using RMS or something else?


        } catch (DAOException e) {
            fail("DAO Exception was thrown: " + e.getMessage());
        } catch (EngineException e) {
            fail("Engine Exception was thrown: " + e.getMessage());
        } catch (TestException e) {
            if (e.isSkippedTest()) {
                System.out.println("Unable to test with even parameters. Test is skipped.");
            } else {
                fail("Unable to perform test!");
            }
        }


    }



    // -----------------------------------------------------------------------------------------------------------------

    // Helper Methods for test class
    private class PredictionEngineOracle {

        /**
         * @param fullUserSubmission    Full UserSubmission (i.e. everything they rated)
         * @return  Returns a Half User Submission (i.e. first half of what they rated)
         * @throws TestException
         */
        private UserSubmission getHalfUserSubmission(UserSubmission fullUserSubmission) throws TestException {

            UserSubmission halfUserSubmission = new UserSubmission(fullUserSubmission.getUserId());

            // User must have at least rated 2 movies
            if (fullUserSubmission.getMoviesViewed().size() < 2) {
                throw new TestException(new Exception("User Rated too few movies, cannot proceed with test!"), true);
            }

            // Split full submission in half
            int halfOfRatings = (int) fullUserSubmission.getMoviesViewed().size() / 2;
            for (int i = 0; i < halfOfRatings; i++) {
                int movieId = fullUserSubmission.getMoviesViewed().get(i);
                double rating = fullUserSubmission.getMovieRating(movieId);
                halfUserSubmission.addMovieRating(movieId, rating);
            }

            return halfUserSubmission;
        }

        /**
         * @param predictions       List of Predictions for a given user
         * @param userSubmission    UserSubmission
         * @return  Returns "true" if the Predictions do NOT include any movies that the user already rated
         */
        private boolean doesNotContainAnyAlreadyRatedMovies(List<Prediction> predictions, UserSubmission userSubmission) {
            for (Prediction prediction : predictions) {
                if (userSubmission.getMoviesViewed().contains(prediction.getMovieId())) return false;
            }
            return true;
        }

        /**
         * @param predictions   List of Predictions for a given user
         * @return  Returns "true" if the Predictions are sorted in order (highest to lowest)
         */
        private boolean hasMovieRatingsInDescOrder(List<Prediction> predictions) {
            double prevPredictionRating = Double.MAX_VALUE;
            for (Prediction prediction : predictions) {
                if (prediction.getPredictedRating() > prevPredictionRating) {
                    return false;
                }
                prevPredictionRating = prediction.getPredictedRating();
            }
            return true;
        }
    }

}