package com.flixr.utils;

import com.flixr.beans.User;
import com.flixr.engine.PredictionEngine;
import com.flixr.exceptions.EngineException;
import com.flixr.interfaces.IPredictionDAO;
import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.flixr.configuration.ApplicationConstants.PRED_ENGINE_THREADS;
import static com.flixr.configuration.ApplicationConstants.REC_ENGINE_THREADS;

/**
 * @author Thomas Thompson
 *
 * Used to launch a Stand Alone version of the PredictionEngineInput Engine
 * Purpose is for backend testing / development
 *
 */
public class PredictionEngineHarness implements IPredictionDAO {

    private PredictionEngine predictionEngine;

    private Set<Integer> movieIdsNotViewedByUserId;
    private UserSubmission userSubmission;

    private double[][] correlationMatrix;
    private HashMap<Integer, Integer> movieIdToMatrixIndex; // MovieId - Index


    public PredictionEngineHarness(UserSubmission userSubmission, Set<Integer> movieIdsNotViewedByUserId, double[][] correlationMatrix, HashMap<Integer, Integer> movieIdToMatrixIndex) {
        this.userSubmission= userSubmission;
        this.movieIdsNotViewedByUserId = movieIdsNotViewedByUserId;
        this.correlationMatrix = correlationMatrix;
        this.movieIdToMatrixIndex = movieIdToMatrixIndex;
    }


    public void generatePrediction() throws EngineException {
        predictionEngine = new PredictionEngine(userSubmission, movieIdsNotViewedByUserId, this);
        predictionEngine.generatePredictions();
    }

    public List<Prediction> getAllPredictions() {
        return predictionEngine.getAllMoviePredictions();
    }


    // Helper Methods to support DAO
    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) {

        // Convert MovieId to MatrixIndex
        int i = movieIdToMatrixIndex.get(movieId_i);
        int j = movieIdToMatrixIndex.get(movieId_j);

        // Return Average Difference
        return correlationMatrix[i][j];
    }

    public void savePredictionsToCSV(String predictionOutputFilePath) throws EngineException {

        System.out.println("Saving Predictions to CSV... ");

        // Get all predictions
        List<Prediction> allPredictions = predictionEngine.getAllMoviePredictions();

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

}
