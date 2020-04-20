package com.flixr.utils.helpers.PredictionEngine;

import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;
import com.flixr.engine.PredictionEngine;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.exceptions.TestException;
import com.flixr.interfaces.IPredictionDAO;
import com.flixr.threads.ReadModelCsvThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flixr.configuration.ApplicationConstants.PRED_ENGINE_THREADS;

/**
 * @author Thomas Thompson
 *
 * Test Driver for Prediction Engine
 */
public class PredictionEngineHarnessTestDriver implements IPredictionDAO {

    // I/O paths
    private String projectPath;
    private String ratingFileName;
    private String correlationMatrixFilePrefix;
    private String userSubmissionFilePath;
    private String predictionFilePrefix;
    private String fullUserSubmissionFilePath;
    private String meanSquareOutputFilePath;


    // Instance Variables
    private int[] listOfUserIdsToTest;
    private double[][] correlationMatrix;
    HashMap<Integer, Integer> movieIdToMatrixIndex;
    Set<Integer> totalMovieIds;


    public PredictionEngineHarnessTestDriver() {

        // Set all Test Driver Variables here:
        // .........................................................................................................
        // List of CSV test files
        ratingFileName = "ml-ratings-imdb-u10"; // 10 users
        listOfUserIdsToTest = new int[] {1,2,3,4,5,6,7,8,9,10}; // list of UserIds within selected file

        // Input CSV File Names
        correlationMatrixFilePrefix = "/src/main/resources/ml-models/model"; // uses the production-grade matrix
        userSubmissionFilePath = "/src/test/resources/predictions/inputs/"; // uses test file archive
        predictionFilePrefix = "/src/test/resources/predictions/outputs/predict-user-";
        meanSquareOutputFilePath = "/src/test/resources/predictions/outputs/prediction-rmse-test.csv";
        // .........................................................................................................

        // Creates I/O paths
        projectPath = System.getProperty("user.dir");
        fullUserSubmissionFilePath = projectPath + userSubmissionFilePath + ratingFileName + ".csv";

    }


    /**
     * Helper Methods to support DAO
     * @param movieId_i     Movie Index "i" in Matrix
     * @param movieId_j     Movie Index "j" in Matrix
     * @return  Preference Difference
     */
    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) throws EngineException {

        try {
            // Convert MovieId to MatrixIndex
            int i = movieIdToMatrixIndex.get(movieId_i);
            int j = movieIdToMatrixIndex.get(movieId_j);

            // Return Average Difference
            return correlationMatrix[i][j];

        } catch (IndexOutOfBoundsException | NullPointerException e) {
//                System.out.println("Unable to find entry i=" + movieId_i +", j=" +movieId_j);
//                System.out.println("Assuming correlation to be 0.");
            return 0;
        }

    }

    /**
     * @return  Returns the Correlation Matrix from the ml-model inputs
     * @throws EngineException
     */
    public void loadCorrelationMatrix() throws EngineException {

        // Determine Unique # of Movies
        System.out.println("Determining Correlation Matrix Size... ");
        totalMovieIds = new TreeSet<>();

        for (int fileNumber = 1; fileNumber <= PRED_ENGINE_THREADS; fileNumber++) {
            String line = null;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(projectPath + correlationMatrixFilePrefix + "-" + fileNumber + "-of-" + PRED_ENGINE_THREADS + ".csv"));
                bufferedReader.readLine(); // skips header row
                while ( (line = bufferedReader.readLine()) != null ) {

                    // Assumes format: (UserId,MovieId,Rating)
                    String[] input = line.split(",");
                    int userId = Integer.parseInt(input[0]);
                    int movieId = Integer.parseInt(input[1]);

                    // Add to list of (unique) sorted MovieIds
                    totalMovieIds.add(movieId);

                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Unable to read input file: \n" + projectPath + correlationMatrixFilePrefix + "-" + fileNumber + "-of-" + PRED_ENGINE_THREADS + ".csv");
                e.printStackTrace();
                throw new EngineException(e);
            } catch (NumberFormatException e) {
                System.out.println("Unable to parse the following line: \n" + line);
                e.printStackTrace();
                throw new EngineException(e);
            }
        }

        // Initialize Matrix & Index Map
        System.out.println("Loading Correlation Matrix... ");
        int totalCountOfMovies = totalMovieIds.size();
        correlationMatrix = new double[totalCountOfMovies][totalCountOfMovies];
        movieIdToMatrixIndex = new HashMap<>();

        // Map MovieId to Matrix Index
        int matrixIndx = 0;
        for (int movieId: totalMovieIds) {
            movieIdToMatrixIndex.put(movieId, matrixIndx);
            matrixIndx++;
        }

        // Reads file, assuming it is in CSV format
        // Spawn Threads for faster predictions (NOTE: # of model.csv files must align with threads!!!)
        ExecutorService executor = Executors.newFixedThreadPool(PRED_ENGINE_THREADS);
        try {
            for (int i = 1; i <= PRED_ENGINE_THREADS; i++) {
                // Read model CSV file x of y
                String currentFileName = projectPath + correlationMatrixFilePrefix + "-" + i + "-of-" + PRED_ENGINE_THREADS + ".csv";
                ReadModelCsvThread readModelCsvThread = new ReadModelCsvThread(i, currentFileName, movieIdToMatrixIndex, correlationMatrix);
                executor.execute(readModelCsvThread);
            }

            // Wait for threads to complete
            executor.shutdown();
            while (!executor.isTerminated()) {}


        } catch (RuntimeException e) {
            System.out.println("Unable to generate Correlation Matrix! Problem encountered within Threads!");
            e.printStackTrace();
            throw new EngineException(e);
        }

        System.out.println("Correlation Matrix Loaded.");
    }

    // Read Ratings File to generate a user submission
    public UserSubmission generateFullUserSubmission(int selectedUserId) {

        System.out.println("Generating User Submission for UserId: " + selectedUserId + "...");

        UserSubmission userSubmission = new UserSubmission(selectedUserId);

        // Reads Model Matrix, assuming it is in CSV format
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fullUserSubmissionFilePath));
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
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Unable to read input file: \n" + fullUserSubmissionFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        }

        System.out.println("User Submission Complete.");
        return userSubmission;
    }


    public Set<Integer> getMovieIdsNotViewedByUserId(UserSubmission userSubmission) {
        Set<Integer> movieIdsNotViewedByUserId = new TreeSet<>();

        // Deep Copy of Total List of MovieIds
        for (int movieId : totalMovieIds) {
            movieIdsNotViewedByUserId.add(movieId);
        }

        // Iterate over list of movie ids and remove any movies that were rated by selected user
        for (int movieId : userSubmission.getMoviesViewed()) {
            movieIdsNotViewedByUserId.remove(new Integer(movieId));
        }

        return movieIdsNotViewedByUserId;
    }


    /**
     * @param userSubmission    Full UserSubmission
     * @return  List of UserSubmissions 1st Half & 2nd Half in positions 0, 1 respectively
     */
    public List<UserSubmission> splitUserSubmissionInHalf(UserSubmission userSubmission, PredictionEngineHarnessTestOracle predictionEngineHarnessTestOracle) throws TestException {

        // User must have at least rated 2 movies
        if (userSubmission.getMoviesViewed().size() < 2) {
            throw new TestException(new Exception("User Rated too few movies, cannot proceed with test!"));
        }

        // Initialize
        int userId = userSubmission.getUserId();
        List<UserSubmission> splitUserSubmissions = new ArrayList<>();
        UserSubmission firstHalfUserSubmission = new UserSubmission(userSubmission.getUserId());
        UserSubmission secondHalfUserSubmission = new UserSubmission(userSubmission.getUserId());

        // Determine Size of First Submission
        int sizeOfFirstSubmission = userSubmission.getMoviesViewed().size() / 2; // int division, same as floor

        // Build 1st submission
        for (int i = 0; i < sizeOfFirstSubmission; i++) {
            int movieId = userSubmission.getMoviesViewed().get(i);
            double movieRating = userSubmission.getMovieRating(movieId);
            firstHalfUserSubmission.addMovieRating(movieId, movieRating);
        }
        splitUserSubmissions.add(firstHalfUserSubmission);

        // Build 2nd submission
        for (int i = sizeOfFirstSubmission; i < userSubmission.getMoviesViewed().size(); i++) {
            int movieId = userSubmission.getMoviesViewed().get(i);
            double movieRating = userSubmission.getMovieRating(movieId);
            secondHalfUserSubmission.addMovieRating(movieId, movieRating);
        }
        splitUserSubmissions.add(secondHalfUserSubmission);

        // Store to Test Oracle
        predictionEngineHarnessTestOracle.getUserIdToCountOfTestMovieMap().put(userId, firstHalfUserSubmission.getMoviesViewed().size());
        predictionEngineHarnessTestOracle.getUserIdToCountOfValidationMovieMap().put(userId, secondHalfUserSubmission.getMoviesViewed().size());

        return splitUserSubmissions;
    }


    /**
     * Run an instance of the Prediction Engine to generate a set of movie predictions
     * @param userSubmission
     * @param movieIdsNotViewedByUserId
     * @throws EngineException
     * @return Returns a (sorted) list of movie predictions
     */
    public List<Prediction> generatePrediction(UserSubmission userSubmission, Set<Integer> movieIdsNotViewedByUserId) throws EngineException {

        // Initialize & Run PredictionEngine
        PredictionEngine predictionEngine = new PredictionEngine(userSubmission, movieIdsNotViewedByUserId, this);
        predictionEngine.generatePredictions();

        // Return list of predictions
        return predictionEngine.getAllMoviePredictions();

    }

    public int[] getListOfUserIdsToTest() {
        return listOfUserIdsToTest;
    }

    public double[][] getCorrelationMatrix() {
        return correlationMatrix;
    }

    public HashMap<Integer, Integer> getMovieIdToMatrixIndex() {
        return movieIdToMatrixIndex;
    }

    public String getFullPredictionOutputPathForGivenUserId(int userId) {
        return projectPath + predictionFilePrefix + userId + ".csv";
    }

    public String getMeanSquareOutputFullFilePath() {
        return projectPath + meanSquareOutputFilePath;
    }


}