package com.flixr.utils.helpers.PredictionEngine;

import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;
import com.flixr.exceptions.EngineException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

/**
 * @author Thomas Thompson
 *
 * Test Oracle for Prediction Engine
 */
public class PredictionEngineHarnessTestOracle {

    // I/O Variables
    private String meanSquareOutputFullFilePath;
    private int[] listOfUserIdsToTest;

    // Reporting Variables
    HashMap<Integer, Integer> userIdToCountOfTestMovieMap = new HashMap<>(); // key = userId, value = CountOfTestMovieIds
    HashMap<Integer, Integer> userIdToCountOfValidationMovieMap = new HashMap<>(); // key = userId, value = CountOfValidationMovieIds
    HashMap<Integer, Double> userIdToMeanSquaredErrorMap = new HashMap<>(); // key = userId, value = MeanSquaredError

    public PredictionEngineHarnessTestOracle() {}

    public void initialize(int[] listOfUserIdsToTest, String meanSquareOutputFullFilePath) {
        this.meanSquareOutputFullFilePath = meanSquareOutputFullFilePath;
        this.listOfUserIdsToTest = listOfUserIdsToTest;
    }


    public void savePredictionsToCSV(List<Prediction> allPredictions, String predictionOutputFilePath) throws EngineException {

        System.out.println("Saving Predictions to CSV... ");

        PrintWriter writer = null;
        try {
            // Make Write & Print Header Row
            writer = new PrintWriter(predictionOutputFilePath, "UTF-8");
            writer.println("MovieID,PredictedRating");

            // Iterate over all predictions
            for (Prediction prediction : allPredictions) {
                writer.println(prediction.getMovieId() + "," + prediction.getPredictedRating());
            }

        } catch (Exception e) {
            EngineException ee = new EngineException(e);
            ee.setEngineMessage("Unable to Save Trained Model.");
            throw ee;

        } finally {
            // Closes CSV output writer
            if (writer != null) writer.close();
        }

        System.out.println("Predictions saved.");
    }


    public void setRootMeanSquaredError(List<Prediction> listOfAllPredictions, UserSubmission validationUserSubmission) {
        // Initialize
        int userId = validationUserSubmission.getUserId();
        int numberOfValidationRatings = 0;
        double sumOfSquaredErrors = 0.0;

        // Iterate over Validation UserSubmission
        for (int movieId : validationUserSubmission.getMoviesViewed()) {
            double actualRating = validationUserSubmission.getMovieRating(movieId); // yi
            double predictedRating = this.getPredictedMovieRating(movieId, listOfAllPredictions); // y^i
            sumOfSquaredErrors += Math.pow( (actualRating - predictedRating), 2);
            numberOfValidationRatings++; // N
        }

        // Calculate RMSE and store to Test Oracle
        double meanSquaredError = sumOfSquaredErrors / numberOfValidationRatings;
        double rootMeanSquaredError = Math.sqrt(meanSquaredError);
        userIdToMeanSquaredErrorMap.put(userId, rootMeanSquaredError);
    }


    // Iterate over list of Predictions to find the Predicted Rating for a given MovieId
    private double getPredictedMovieRating(int movieId, List<Prediction> listOfPredictions) {

        // Find the predicted rating
        for (Prediction prediction : listOfPredictions) {
            if (prediction.getMovieId() == movieId) {
                double predictedRating = prediction.getPredictedRating();
                // Note that our Predicted Ratings were able to go over 5.0, so will cap them at 5.0
                // Otherwise, something like 5.4 will trigger a RSME violation later
                if (predictedRating > 5.0) {
                    predictedRating = 5.0;
                }
                // Note we will also round our prediction to the nearest 0.5
                predictedRating = Math.round(predictedRating * 2.0) / 2.0;
                return predictedRating;
            }
        }

        // Rating not in list, very large negative value to trigger MSE issue
        return Double.MIN_VALUE;
    }


    public void printRootMeanSquaredAnalysis() throws IOException {

        // Display MSE accuracy
        System.out.println("Root Mean Square Results...");

        // Make Write & Print Header Row
        PrintWriter writer = new PrintWriter(meanSquareOutputFullFilePath, "UTF-8");
        String csvHeaderEntry = "UserId, CountOfTestMovieIds, CountOfValidationMovieIds, RootMeanSquaredError";
        System.out.println(csvHeaderEntry);
        writer.println(csvHeaderEntry);
        for (int userId : listOfUserIdsToTest) {
            String csvRowEntry = userId + ", " +
                    userIdToCountOfTestMovieMap.get(userId) + ", " +
                    userIdToCountOfValidationMovieMap.get(userId) + ", " +
                    userIdToMeanSquaredErrorMap.get(userId);
            System.out.println(csvRowEntry);
            writer.println(csvRowEntry);
        }

        // Closes CSV output writer
        writer.close();

    }

    public HashMap<Integer, Integer> getUserIdToCountOfTestMovieMap() {
        return userIdToCountOfTestMovieMap;
    }

    public HashMap<Integer, Integer> getUserIdToCountOfValidationMovieMap() {
        return userIdToCountOfValidationMovieMap;
    }

    public HashMap<Integer, Double> getUserIdToMeanSquaredErrorMap() {
        return userIdToMeanSquaredErrorMap;
    }

}