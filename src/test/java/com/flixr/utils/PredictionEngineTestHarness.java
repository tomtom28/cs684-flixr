package com.flixr.utils;

import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;

import com.flixr.utils.helpers.PredictionEngine.PredictionEngineHarnessTestDriver;
import com.flixr.utils.helpers.PredictionEngine.PredictionEngineHarnessTestOracle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Testing of the PredictionEngine as a StandAlone Sub-System
 *
 *  Used to launch a Stand Alone version of the PredictionEngine
 *  Purpose is for backend testing / development
 */
public class PredictionEngineTestHarness {

    private PredictionEngineHarnessTestDriver predictionEngineHarnessTestDriver;
    private PredictionEngineHarnessTestOracle predictionEngineHarnessTestOracle;

    // Create a new Oracle for Standalone testing
    @BeforeEach
    public void initEach() {
        predictionEngineHarnessTestDriver = new PredictionEngineHarnessTestDriver();
        predictionEngineHarnessTestOracle = new PredictionEngineHarnessTestOracle();
    }

    /**
     * Thomas Thompson, Test # 3
     * Test Name: Unit-3
     *
     * Runs the PredictionEngine against various User Submissions (full user submissions, using all data)
     *
     * All User Submissions will be Stored to CSV files for further analysis
     * This can also serve as an Integration Test
     */
    @Test
    void testPredictionGenerationForGivenUserIds() {
        try {

            // Load Correlation Matrix
            predictionEngineHarnessTestDriver.loadCorrelationMatrix();

            // Iterate over all UserIds in Test Driver
            for (int userId : predictionEngineHarnessTestDriver.getListOfUserIdsToTest()) {

                // Generate User Submission for given UserId
                UserSubmission userSubmission = predictionEngineHarnessTestDriver.generateFullUserSubmission(userId);
                Set<Integer> movieIdsNotViewedByUserId = predictionEngineHarnessTestDriver.getMovieIdsNotViewedByUserId(userSubmission);


                // Generate Prediction
                List<Prediction> listOfPredictions = predictionEngineHarnessTestDriver.generatePrediction(userSubmission, movieIdsNotViewedByUserId);

                // Save All Predictions for the given UserId
                predictionEngineHarnessTestOracle.savePredictionsToCSV(listOfPredictions, predictionEngineHarnessTestDriver.getFullPredictionOutputPathForGivenUserId(userId));
            }


        } catch (Exception e) {
            e.printStackTrace();
            fail("Error was thrown: " + e.getMessage());
        }
    }


    /**
     * Thomas Thompson, Test # 4
     * Test Name: Unit-4
     *
     * Assess the accuracy of predictions for a given set of userIds
     *
     * Uses the Following Approach:
     * 1) Split all the user's Ratings for a given user in half
     * 2) Generate Movie Predictions for the first half of the Rated Movies
     * 3) Compare the Predicted Rating against Actual Rating for second half of the Rated Movies
     *
     * Note:
     * The Comparision will use Root Mean Squared Error
     * https://link.medium.com/6GkvNdRQD5
     */
    @Test
    void testPredictionAccuracyForGivenUserIds() {

        try {

            // Load Correlation Matrix
            predictionEngineHarnessTestDriver.loadCorrelationMatrix();

            // Iterate over all UserIds in Test Driver
            for (int userId : predictionEngineHarnessTestDriver.getListOfUserIdsToTest()) {

                // Generate Full User Submission for given UserId
                UserSubmission fullUserSubmission = predictionEngineHarnessTestDriver.generateFullUserSubmission(userId);

                // Split UserSubmissions into Test & Validation Portions
                UserSubmission testHalfUserSubmission = predictionEngineHarnessTestDriver.splitUserSubmissionInHalf(fullUserSubmission, predictionEngineHarnessTestOracle).get(0); // 1st half for testing
                UserSubmission validationHalfUserSubmission = predictionEngineHarnessTestDriver.splitUserSubmissionInHalf(fullUserSubmission, predictionEngineHarnessTestOracle).get(1); // 2nd half for validation

                // List of MovieIds not rated by the user's test portion (i.e. includes validation movies)
                Set<Integer> testListOfMovieIdsNotViewedByUserId = predictionEngineHarnessTestDriver.getMovieIdsNotViewedByUserId(testHalfUserSubmission);

                // Compare Validation Half UserSubmission Against Predictions
                List<Prediction> allPredictions = predictionEngineHarnessTestDriver.generatePrediction(testHalfUserSubmission, testListOfMovieIdsNotViewedByUserId);
                predictionEngineHarnessTestOracle.setRootMeanSquaredError(allPredictions, validationHalfUserSubmission);

            }

            // Save & Display Test Results
            predictionEngineHarnessTestOracle.initialize(predictionEngineHarnessTestDriver.getListOfUserIdsToTest(), predictionEngineHarnessTestDriver.getMeanSquareOutputFullFilePath());
            predictionEngineHarnessTestOracle.printRootMeanSquaredAnalysis();


        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to complete prediction! Error was thrown: " + e.getMessage());
        }

    }

}