package com.flixr.engine.utils;

import com.flixr.engine.PredictionEngine;
import com.flixr.engine.exceptions.EngineException;
import com.flixr.engine.io.IPredictionEngineDAO;
import com.flixr.engine.io.Prediction;
import com.flixr.engine.io.UserSubmission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * Used to launch a Stand Alone version of the PredictionEngineInput Engine
 * Purpose is for backend testing / development
 *
 */
public class PredictionEngineHarness implements IPredictionEngineDAO {

    PredictionEngine predictionEngine;
    private String ratingInputFilePath;
    private String modelInputFilePath;

    private Set<Integer> totalMovieIds;
    private Set<Integer> movieIdsNotViewedByUserId;
    private UserSubmission userSubmission;

    private int totalCountOfMovies;

    private double[][] correlationMatrix;
    private HashMap<Integer, Integer> movieIdToMatrixIndex; // MovieId - Index


    public PredictionEngineHarness(String ratingInputFilePath, String modelInputFilePath) {
        this.ratingInputFilePath = ratingInputFilePath;
        this.modelInputFilePath = modelInputFilePath;
    }


    // Read Ratings File to generate a user submission
    private void generateUserSubmission(int selectedUserId) {

        System.out.println("Generating User Submission for UserId: " + selectedUserId + "...");

        userSubmission = new UserSubmission(selectedUserId);
        totalMovieIds = new TreeSet<>();
        movieIdsNotViewedByUserId = new TreeSet<>();

        // Reads Model Matrix, assuming it is in CSV format
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(ratingInputFilePath));
            bufferedReader.readLine(); // skips header row
            while ( (line = bufferedReader.readLine()) != null ) {

                // Assumes format: (UserId,MovieId,Rating)
                String[] input = line.split(",");
                int userId = Integer.parseInt(input[0]);
                int movieId = Integer.parseInt(input[1]);
                double rating = Double.parseDouble(input[2]);

                // If current UserId matches selected userId, then add to submission
                if (userId == selectedUserId) {
                    userSubmission.addMovieRating(movieId, rating);
                }

                // Add to list of (unique) sorted MovieIds
                totalMovieIds.add(movieId);
                movieIdsNotViewedByUserId.add(movieId);

            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Unable to read input file: \n" + ratingInputFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        }

        // Collect Total # of Movies
        totalCountOfMovies = totalMovieIds.size();

        // Iterate over list of movie ids and remove any movies that were rated by selected user
        for (int movieId : userSubmission.getMoviesViewed()) {
            movieIdsNotViewedByUserId.remove(new Integer(movieId));
        }

        System.out.println("User Submission Complete.");

    }


    public void generateMatrixModel() {

        // Track Progress
        System.out.println("Loading Correlation Matrix... ");

        // Initialize Matrix & Index Map
        correlationMatrix = new double[totalCountOfMovies][totalCountOfMovies];
        movieIdToMatrixIndex = new HashMap<>();

        // Map MovieId to Matrix Index
        int matrixIndx = 0;
        for (int movieId: totalMovieIds) {
            movieIdToMatrixIndex.put(movieId, matrixIndx);
            matrixIndx++;
        }

        // Reads file, assuming it is in CSV format
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(modelInputFilePath));
            bufferedReader.readLine(); // skips header row
            while ( (line = bufferedReader.readLine()) != null ) {

                // Assumes format: (MovieId_i,MovieId_j,Rating)
                String[] input = line.split(",");
                int movieIdMatrix_i = Integer.parseInt(input[0]);
                int movieIdMatrix_j = Integer.parseInt(input[1]);
                double avgRatingDifference = Double.parseDouble(input[2]);

                // Convert MovieId to Matrix Index
                int i = movieIdToMatrixIndex.get(movieIdMatrix_i);
                int j = movieIdToMatrixIndex.get(movieIdMatrix_j);

                // Add to internal matrix
                correlationMatrix[i][j] = avgRatingDifference;

            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Unable to read input file: \n" + modelInputFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        } catch (NullPointerException e ) {
            e.printStackTrace();
            System.out.println("Unable to parse the following line: \n" + line + "\n" +
            "Likely issue: The movieIdToMatrixIndex may be missing a movieId. \n" +
            "Possible fix: Ensure that the model.csv and ratings.csv are aligned.");
        }

        System.out.println("Correlation Matrix Loaded.");

    }


    private void generatePrediction() {
        predictionEngine = new PredictionEngine(userSubmission, movieIdsNotViewedByUserId, this);
        predictionEngine.generatePredictions();
    }


    // Helper Methods to support DAO
    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) {

        // Convert MovieId to MatrixIndex
        int i = movieIdToMatrixIndex.get(movieId_i);
        int j = movieIdToMatrixIndex.get(movieId_j);

        // Return Average Difference
        return correlationMatrix[i][j];
    }

    private void savePredictionsToCSV(String predictionOutputFilePath) throws EngineException {

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


    // Creates an PredictionEngineHarness for Standalone model training
    public static void main(String[] args) throws EngineException {

        // Select a UserId to Predict Movies
        int userId = 2;

        // Select Training File Name
        String ratingFileName = "ml-small-ratings";

        // Selects a Input/Output CSV Files
        String path = System.getProperty("user.dir");
        String ratingInputFile = "/test_data/" + ratingFileName + ".csv";
        String modelInputFile = "/test_data/outputs/models/model-" + ratingFileName + ".csv";
        String predictionOutputFile = "/test_data/outputs/predictions/prediction-" + ratingFileName + "-user-" + userId + ".csv";

        // Create Prediction Engine Harness
        PredictionEngineHarness predictionEngineHarness = new PredictionEngineHarness(path + ratingInputFile, path + modelInputFile);

        // Generate User Submission for given UserId
        predictionEngineHarness.generateUserSubmission(userId);

        // Generate Matrix Model
        predictionEngineHarness.generateMatrixModel();

        // Generate Prediction
        predictionEngineHarness.generatePrediction();

        // Print All Predictions
        predictionEngineHarness.savePredictionsToCSV(path + predictionOutputFile);

    }

}
