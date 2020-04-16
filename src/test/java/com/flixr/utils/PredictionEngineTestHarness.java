package com.flixr.utils;

import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;
import com.flixr.engine.PredictionEngine;
import com.flixr.exceptions.EngineException;
import com.flixr.exceptions.TestException;
import com.flixr.interfaces.IPredictionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.PRED_ENGINE_THREADS;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Testing of the PredictionEngine as a StandAlone Sub-System
 *
 *  Used to launch a Stand Alone version of the PredictionEngine
 *  Purpose is for backend testing / development
 */
class PredictionEngineTestHarness {

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


    // -----------------------------------------------------------------------------------------------------------------

    // Test Driver
    private class PredictionEngineHarnessTestDriver implements IPredictionDAO {

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
            listOfUserIdsToTest = new int[] {1,2,3,4,5,6,7,8,9,10}; // list of UserIds within selected list

            // Input CSV File Names
            correlationMatrixFilePrefix = "/src/main/resources/ml-models/model"; // uses the production-grade matrix
            userSubmissionFilePath = "/src/test/resources/ml-models/inputs/";
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
                System.out.println("Unable to find entry i=" + movieId_i +", j=" +movieId_j);
                System.out.println("Assuming correlation to be 0.");
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
            for (int fileNumber = 1; fileNumber <= PRED_ENGINE_THREADS; fileNumber++) {
                String line = null;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(projectPath + correlationMatrixFilePrefix + "-" + fileNumber + "-of-" + PRED_ENGINE_THREADS + ".csv"));
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
                    System.out.println("Unable to read input file: \n" + projectPath + correlationMatrixFilePrefix + "-" + fileNumber + "-of-" + PRED_ENGINE_THREADS + ".csv");
                    e.printStackTrace();
                    throw new EngineException(e);
                } catch (NumberFormatException e) {
                    System.out.println("Unable to parse the following line: \n" + line);
                    e.printStackTrace();
                    throw new EngineException(e);
                }
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


    // -----------------------------------------------------------------------------------------------------------------

    // Test Oracle
    private class PredictionEngineHarnessTestOracle {

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

}