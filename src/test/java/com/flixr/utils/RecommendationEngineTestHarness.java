package com.flixr.utils;

import com.flixr.beans.UserSubmission;
import com.flixr.exceptions.EngineException;
import com.flixr.threads.RecEngineThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flixr.configuration.ApplicationConstants.REC_ENGINE_THREADS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 *
 * Unit Tests related to running the RecommendationEngine as a Stand Alone Sub-System
 *
 * Used to launch a Stand Alone version of the Recommendation Engine
 * Purpose is for backend testing / development
 */
public class RecommendationEngineTestHarness {

    private RecommendationEngineHarnessTestDriver recommendationEngineHarnessTestDriver;
    private RecommendationEngineHarnessTestOracle recommendationEngineHarnessTestOracle;

    // Create a new Oracle for Standalone testing
    @BeforeEach
    public void initEach() {
        recommendationEngineHarnessTestDriver = new RecommendationEngineHarnessTestDriver();
        recommendationEngineHarnessTestOracle = new RecommendationEngineHarnessTestOracle();
    }

    /**
     * Thomas Thompson, Test # 1
     * Test Name: Unit-1
     *
     * Ensure that RecommendationEngine Runs & Generates a Valid Correlation Matrix
     *
     * Heuristic Based Approach:
     * 1) Spot check that Matrix diagonal only has zeros
     * 2) Spot check that (i,j) and (j,i) have the same magnitude but are negatives of each other
     */
    @Test
    void testValidMatrixForRecommendationEngineHarness() {

        int numberOfMatrixDiagonalTests = 10;
        int numberOfMatrixSymmetryTests = 100;

        try {
            // Call Test Driver to generate input objects
            recommendationEngineHarnessTestDriver.readInputFile(recommendationEngineHarnessTestDriver.getDefaultInputFile());

            // Train Model & Generate Correlation Matrix
            recommendationEngineHarnessTestDriver.trainModel();

            // Load Trained Model (i.e. Correlation Matrix)
            recommendationEngineHarnessTestDriver.loadCorrelationMatrix();

            // Step 1 - Test N number of random positions along matrix diagonal
            recommendationEngineHarnessTestOracle.validateMatrixDiagonals(numberOfMatrixDiagonalTests);

            // Step 2 - Test N number of random mirrored positions (i,j) = -(j,i)
            recommendationEngineHarnessTestOracle.validateMatrixSymmetry(numberOfMatrixSymmetryTests);

        } catch (Exception e) {
            fail("Unable to complete run! Error was thrown: " + e.getMessage());
        }
    }


    /**
     * Thomas Thompson, Test # 2
     * Test Name: Unit-2
     *
     * Run RecommendationEngineHarness to assess run time performance
     * Test Oracle will Compare these run times using a trend line against the O(n) Time Complexity
     */
    @Test
    void testRunTimePerformanceForRecommendationEngineHarness() {
        try {

            // Iterate over all input files from Test Driver
            for (String fileName : recommendationEngineHarnessTestDriver.getInputFiles()) {

                // Call Test Driver to generate input objects
                recommendationEngineHarnessTestDriver.readInputFile(recommendationEngineHarnessTestDriver.getProjectPath() + fileName);

                // Train Model & Generate Correlation Matrix
                double runTimeInSeconds = recommendationEngineHarnessTestDriver.trainModel();

                // Record performance into Oracle
                recommendationEngineHarnessTestOracle.addRunTimePerformanceEntry(fileName, runTimeInSeconds);
            }

            // Calculate Input File Properties
            recommendationEngineHarnessTestOracle.intialize(
                    recommendationEngineHarnessTestDriver.projectPath,
                    recommendationEngineHarnessTestDriver.performanceFilePath,
                    recommendationEngineHarnessTestDriver.ratingFileNames
            );
            recommendationEngineHarnessTestOracle.analyzeInputFiles();

            // Save & Display Run Time Performance Results
            recommendationEngineHarnessTestOracle.printRunTimePerformanceResults();

        } catch (Exception e) {
            fail("Unable to complete run! Error was thrown: " + e.getMessage());
        }
    }


    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @author Thomas Thompson
     *
     * Test Driver for Recommendation Engine
     */
    class RecommendationEngineHarnessTestDriver {

        // I/O paths
        private String projectPath;
        private String[] ratingFileNames;
        private String[] inputFiles;
        private String outputFilePrefix;
        private String performanceFilePath;

        // Instance Variables
        private TreeSet<Integer> sortedListOfAllMovieIds = new TreeSet<>();
        private TreeMap<Integer, UserSubmission> sortedListOfUserSubmissions = new TreeMap<>();

        private double[][] correlationMatrix;
        private HashMap<String,String> ratingFileNameToFullInputFileNameMap = new HashMap<>();


        public RecommendationEngineHarnessTestDriver() {

            // Set all Oracle Variables here:
            // .........................................................................................................

            // List of CSV test files
            ratingFileNames = new String[] {
                "ml-ratings-u5", // 5 users
                "ml-ratings-u10", // 10 users
                "ml-ratings-u25", // 25 users
                "ml-ratings-u50", // 50 users
                "ml-ratings-u100" // 100 users
            };

            // Input CSV File Name, with format: (UserId, MovieId, Rating)
            String inputFilePath = "/src/test/resources/ml-models/inputs/";
            String outputFilePath = "/src/test/resources/ml-models/outputs/";
            String performanceFileName = "performance-test-" + REC_ENGINE_THREADS + "-threads.csv";
            // .........................................................................................................


            // Creates I/O paths
            projectPath = System.getProperty("user.dir");
            inputFiles = new String[ratingFileNames.length];
            for (int i=0; i < inputFiles.length; i++) {
                inputFiles[i] = inputFilePath + ratingFileNames[i] + ".csv";
                ratingFileNameToFullInputFileNameMap.put(ratingFileNames[i],inputFiles[i]);
            }
            outputFilePrefix = outputFilePath + "model";
            performanceFilePath = outputFilePath + performanceFileName;


        }

        // Reads file, assuming it is in CSV format
        public void readInputFile(String inputFilePath) throws EngineException {
            String line = null;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(inputFilePath));
                bufferedReader.readLine(); // skips header row
                while ( (line = bufferedReader.readLine()) != null ) {

                    // Assumes format: (UserId,MovieId,Rating)
                    String[] input = line.split(",");
                    int userId = Integer.parseInt(input[0]);
                    int movieId = Integer.parseInt(input[1]);
                    double rating = Double.parseDouble(input[2]);

                    // Process current entry into List of UserSubmissions
                    processRating(userId, movieId, rating);

                    // Add to list of (unique) sorted MovieIds
                    sortedListOfAllMovieIds.add(movieId);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Unable to read input file: \n" + inputFilePath);
                e.printStackTrace();
                throw new EngineException(e);
            } catch (NumberFormatException e) {
                System.out.println("Unable to parse the following line: \n" + line);
                e.printStackTrace();
                throw new EngineException(e);
            }
        }

        // Maps all UserIds to submitted (Movie, Rating) pairs
        // NOTE: recEngineInputs must be sorted by UserId
        public void processRating(int userId, int movieId, double rating) {

            // Determine current User Submission
            UserSubmission userSubmission;
            if (!sortedListOfUserSubmissions.keySet().contains(userId)) { // New UserId was found
                userSubmission = new UserSubmission(userId);
                sortedListOfUserSubmissions.put(userId, userSubmission);
            }
            else { // Append to existing UserId entry
                userSubmission = sortedListOfUserSubmissions.get(userId);
            }

            // Add (MovieId, Rating) tuple to User Submission
            userSubmission.addMovieRating(movieId,rating);
        }

        /**
         * Trains the ML model (and saves matrix to CSV)
         */
        public double trainModel() {

            long startTime = System.currentTimeMillis();

            // Split Up List of MovieIds
            int[] splitIndxs = getMatrixSplitPoints(this.sortedListOfAllMovieIds);
            List<Integer> listOfDistinctMovieIds = new ArrayList<>(this.sortedListOfAllMovieIds);

            // Run MultiThreaded
            ExecutorService executor = Executors.newFixedThreadPool(REC_ENGINE_THREADS);
            for (int i = 1; i <= REC_ENGINE_THREADS; i++) {
                // Create a subset of the movies for Rec Engine Thread
                TreeSet<Integer> subsetOfDistinctMovieIds = new TreeSet<>( listOfDistinctMovieIds.subList(splitIndxs[i-1], splitIndxs[i]) ) ;

                // Create New Output File for given Thread
                String currentOutputFilePath = projectPath + outputFilePrefix + "-" + i +"-of-" + REC_ENGINE_THREADS + ".csv";

                // Instantiate the given Engine Thread
                RecEngineThread recEngineThread = new RecEngineThread(i, subsetOfDistinctMovieIds, this.sortedListOfAllMovieIds, this.sortedListOfUserSubmissions, currentOutputFilePath);

                // Run a service to compute a subset of the matrix, using the given Engine Thread
                executor.execute(recEngineThread);
            }

            // Wait for threads to complete
            executor.shutdown();
            while (!executor.isTerminated()) {}

            // Logs Run Time
            long endTime = System.currentTimeMillis();
            double totalRunTime = (endTime - startTime)/1000.0;
            System.out.println("\nTotal Run Time: " + totalRunTime + " seconds.");
            return totalRunTime;
        }

        // Copied from RecommendationController
        private int[] getMatrixSplitPoints(TreeSet<Integer> sortedListOfMovieIds) {
            // Determine # of entries per subset of movie list (partitioned by thread count)
            int dividedCount = 1;
            try {
                dividedCount = (int) Math.floor( (double) sortedListOfMovieIds.size() / REC_ENGINE_THREADS ) ;
            } catch (ArithmeticException e) {
                System.out.println("Warning: Number of Recommendation Engine Threads must be at least 1!");
                System.out.println("Proceeding with a single-threaded configuration: REC_ENGINE_THREADS = 1");
            }

            // Determine split indices for new movie list sublists
            int currentIndx = 0;
            int[] splitIndxs = new int[REC_ENGINE_THREADS + 1];
            for (int i = 0; i < splitIndxs.length - 1; i++) {
                splitIndxs[i] = currentIndx;
                currentIndx += dividedCount;
            }
            splitIndxs[splitIndxs.length - 1] = sortedListOfMovieIds.size(); // any remainders will just get tacked on to the last thread

            return splitIndxs;
        }

        /**
         * @return  Returns the Correlation Matrix from the ml-model outputs
         * @throws EngineException
         */
        public void loadCorrelationMatrix() throws EngineException {

            // Determine Unique # of Movies
            System.out.println("Determining Correlation Matrix Size... ");
            Set<Integer> totalMovieIds = new TreeSet<>();

            for (int fileNumber = 1; fileNumber <= REC_ENGINE_THREADS; fileNumber++) {
                String line = null;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(projectPath + outputFilePrefix + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv"));
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
                    System.out.println("Unable to read input file: \n" + projectPath + outputFilePrefix + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv");
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
            HashMap<Integer, Integer> movieIdToMatrixIndex = new HashMap<>();

            // Map MovieId to Matrix Index
            int matrixIndx = 0;
            for (int movieId: totalMovieIds) {
                movieIdToMatrixIndex.put(movieId, matrixIndx);
                matrixIndx++;
            }

            // Reads file, assuming it is in CSV format
            for (int fileNumber = 1; fileNumber <= REC_ENGINE_THREADS; fileNumber++) {
                String line = null;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(projectPath + outputFilePrefix + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv"));
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
                    System.out.println("Unable to read input file: \n" + projectPath + outputFilePrefix + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv");
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

        public String getDefaultInputFile() {
            return projectPath + inputFiles[1];
        }

        public String getProjectPath() {
            return projectPath;
        }

        public String[] getInputFiles() {
            return inputFiles;
        }

        public String getOutputFilePrefix() {
            return outputFilePrefix;
        }


        public HashMap<String,String> getRatingFileNameToFullInputFileNameMap() {
            return ratingFileNameToFullInputFileNameMap;
        }

        public String[] getRatingFileNames() {
            return ratingFileNames;
        }


        /**
         * @return  Returns random (i, i) diagonal value within Correlation Matrix
         */
        public double getRandomMatrixDiagonalValue() {

            // Matrix must be initialized
            if (correlationMatrix == null) {
                return -1.0;
            }

            // Diagonal occurs where (i = j) aka (i, i)
            int i = getRandomMatrixIndex();
            System.out.println("Generated Matrix Diagonal: i=" + i + " and j=" + i);
            return correlationMatrix[i][i]; // matrix should be NxN
        }

        /**
         * @return  Returns value within Correlation Matrix Range
         */
        public int getRandomMatrixIndex() {

            // Matrix must be initialized
            if (correlationMatrix == null) {
                return -1;
            }

            int matrixSize = correlationMatrix[0].length; // matrix should be NxN
            Random r = new Random();
            return r.nextInt(matrixSize);
        }

        /**
         * @param i Matrix Index i
         * @param j Matrix Index j
         * @return  Returns value at Correlation Matrix position (i, j)
         */
        public double getMatrixValue(int i, int j) {

            // Matrix must be initialized
            if (correlationMatrix == null) {
                return -1.0;
            }

            return correlationMatrix[i][j];
        }

        /**
         * @param i Matrix Index i
         * @param j Matrix Index j
         * @return  Returns expected mirrored value at Correlation Matrix position (i, j); i.e. (-1 * (j, i))
         */
        public double getMirroredMatrixValue(int i, int j) {

            // Matrix must be initialized
            if (correlationMatrix == null) {
                return -1.0;
            }

            double mirroredValue = -1.0 * correlationMatrix[j][i];
            System.out.println("Generated Matrix Mirror: (" + i + ", " + j + ") -> (" + j  + ", " + i + ")");
            if (mirroredValue == -0.0) return 0.0; // Handle -0.0 issue
            else return mirroredValue;

        }

    }


    // -----------------------------------------------------------------------------------------------------------------


    /**
     * @author Thomas Thompson
     *
     * Test Oracle for Recommendation Engine
     */
    class RecommendationEngineHarnessTestOracle {

        // I/O paths
        String[] ratingFileNames;
        String projectPath;
        String performanceFilePath;

        // Keys for HashMap Analysis
        String MOVIES_KEY = "<>NUM_MOVIES";
        String USERS_KEY = "<>NUM_USERS";
        String RATINGS_KEY = "<>" + "NUM_RATINGS";
        String TREND_KEY = "<>" + "N2xM";

        // Instance Variables for Reporting
        private HashMap<String,Double> runTimePerformanceMap; // key = fullFileName, value = real run time
        private HashMap<String,Double> ratingFileAnalysisMap; // key = fullFileName<>ATTRIBUTE
        private HashMap<String,Double> theoreticalRunTimePerformanceMap; // key = fullFileName, value = theoretical run time (normalized)

        public RecommendationEngineHarnessTestOracle() {}


        public void intialize(String projectPath, String performanceFilePath, String[] ratingFileNames) {
            this.projectPath = projectPath;
            this.performanceFilePath = performanceFilePath;
            this.ratingFileNames = ratingFileNames;
        }

        /**
         * Test Condition
         * Validates that a given # of randomly selected matrix diagonal positions are 0, i.e. no correlation from movie i to movie i
         * @param numberOfMatrixDiagonalTests
         */
        public void validateMatrixDiagonals(int numberOfMatrixDiagonalTests) {
            for (int i = 0; i < numberOfMatrixDiagonalTests; i++) {
                double matrixDiagonalValue = recommendationEngineHarnessTestDriver.getRandomMatrixDiagonalValue();
                assertEquals(0.0, matrixDiagonalValue, "Correlation Matrix Diagonal values must be 0.0!");
                System.out.println("Tested " + (i+1) + " of " + (numberOfMatrixDiagonalTests + 1));
            }
        }

        /**
         * Test Condition
         * Validates N number of random mirrored positions (i,j) = -(j,i)
         * @param numberOfMatrixSymmetryTests
         */
        public void validateMatrixSymmetry(int numberOfMatrixSymmetryTests) {
            System.out.println("\nConfirming Matrix Symmetry...");
            for (int i = 0; i < numberOfMatrixSymmetryTests; i++) {
                int pos_i = recommendationEngineHarnessTestDriver.getRandomMatrixIndex();
                int pos_j = recommendationEngineHarnessTestDriver.getRandomMatrixIndex();
                double matrixValue_i_j = recommendationEngineHarnessTestDriver.getMatrixValue(pos_i, pos_j);
                assertEquals(matrixValue_i_j, recommendationEngineHarnessTestDriver.getMirroredMatrixValue(pos_i, pos_j), "Correlation Matrix must be symmetrical for (" + pos_i + ", " + pos_j + ") and (" + pos_j + ", " + pos_i + ")");
                System.out.println("Tested " + (i+1) + " of " + (numberOfMatrixSymmetryTests + 1) + "... Matching Value = " + matrixValue_i_j);
            }

        }

        // Read all input files in Test Oracle and collect various attributes
        public void analyzeInputFiles() throws EngineException {

            ratingFileAnalysisMap = new HashMap<>();

            for (String ratingFileName : ratingFileNames) {

                // Track Total Number of Movies, Users, & Ratings
                int ratingCount = 0;
                TreeSet<Integer> sortedListOfAllMovieIds = new TreeSet<>();
                TreeSet<Integer> sortedListOfAllUserIds = new TreeSet<>();

                String line = null;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(recommendationEngineHarnessTestDriver.getProjectPath() +
                            recommendationEngineHarnessTestDriver.getRatingFileNameToFullInputFileNameMap().get(ratingFileName)));
                    bufferedReader.readLine(); // skips header row
                    while ((line = bufferedReader.readLine()) != null) {

                        // Assumes format: (UserId,MovieId,Rating)
                        String[] input = line.split(",");
                        int userId = Integer.parseInt(input[0]);
                        int movieId = Integer.parseInt(input[1]);
                        double rating = Double.parseDouble(input[2]);

                        // Add list of (unique) UserIds
                        sortedListOfAllUserIds.add(userId);

                        // Add to list of (unique) sorted MovieIds
                        sortedListOfAllMovieIds.add(movieId);

                        // Increment number of entries (ie ratings)
                        ratingCount++;
                    }

                    // Get various attributes
                    double numberOfMovies = sortedListOfAllMovieIds.size();
                    double numberOfUsers = sortedListOfAllUserIds.size();
                    double theoreticalTrendFactor = numberOfMovies*numberOfMovies*numberOfUsers; // O(n) = movies^2 * users

                    // Set attributes
                    String numberOfMoviesKey = ratingFileName + MOVIES_KEY;
                    ratingFileAnalysisMap.put(numberOfMoviesKey, numberOfMovies);
                    String numberOfUsersKey = ratingFileName + USERS_KEY;
                    ratingFileAnalysisMap.put(numberOfUsersKey, numberOfUsers);
                    String numberOfRatingsKey = ratingFileName + RATINGS_KEY;
                    ratingFileAnalysisMap.put(numberOfRatingsKey, ratingCount*1.0);
                    String trendFactorKey = ratingFileName + TREND_KEY;
                    ratingFileAnalysisMap.put(trendFactorKey, theoreticalTrendFactor);

                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.println("Unable to read input file: \n" + recommendationEngineHarnessTestDriver.getRatingFileNameToFullInputFileNameMap().get(ratingFileName));
                    e.printStackTrace();
                    throw new EngineException(e);
                } catch (NumberFormatException e) {
                    System.out.println("Unable to parse the following line: \n" + line);
                    e.printStackTrace();
                    throw new EngineException(e);
                }
            }
        }


        /**
         * Inserts the FileName & Run Time for a given Performance Test Iteration
         * @param fileName
         * @param runTimeInSeconds
         */
        public void addRunTimePerformanceEntry(String fileName, double runTimeInSeconds) {
            if (this.runTimePerformanceMap == null) {
                runTimePerformanceMap = new HashMap<>();
            }
            runTimePerformanceMap.put(fileName, runTimeInSeconds);
        }

        public void printRunTimePerformanceResults() throws IOException {

            // Normalize Theoretical Run Times against Real Run Time
            double normalizationFactor = this.calculateNormalizationFactor();
            setTheoreticalRunTimePerformanceMap(normalizationFactor);

            // Display Run Times
            System.out.println("Run Time Performance Results...");

            // Make Write & Print Header Row
            PrintWriter writer = new PrintWriter( projectPath + performanceFilePath, "UTF-8");
            String csvHeaderEntry = "FileName, ActualRunTime (sec), NumberOfMovies, NumberOfUsers, NumberOfRatings, n^2*m, TheoreticalRunTime (sec), Normalization Factor = " + normalizationFactor;
            System.out.println(csvHeaderEntry);
            writer.println(csvHeaderEntry);
            for (String ratingFileName : ratingFileNames) {
                String csvRowEntry = ratingFileName + ", " +
                        runTimePerformanceMap.get(recommendationEngineHarnessTestDriver.getRatingFileNameToFullInputFileNameMap().get(ratingFileName)) + ", " +
                        ratingFileAnalysisMap.get(ratingFileName+MOVIES_KEY) + ", " +
                        ratingFileAnalysisMap.get(ratingFileName+USERS_KEY) + ", " +
                        ratingFileAnalysisMap.get(ratingFileName+RATINGS_KEY) + ", " +
                        ratingFileAnalysisMap.get(ratingFileName+TREND_KEY) + ", " +
                        theoreticalRunTimePerformanceMap.get(ratingFileName);
                System.out.println(csvRowEntry);
                writer.println(csvRowEntry);
            }

            // Closes CSV output writer
            writer.close();
        }

        // Calculates the Theoretical Run Times by normalizing the trend factors
        private double calculateNormalizationFactor() {

            // Find File with most ratings
            String selectedFileName = ratingFileNames[0];
            double currentLargestRatingCount = ratingFileAnalysisMap.get(ratingFileNames[0] + RATINGS_KEY);
            for (int i = 1; i < ratingFileNames.length; i++) {
                // Update values if next file has more ratings
                double numberOfRatings = ratingFileAnalysisMap.get(ratingFileNames[i] + RATINGS_KEY);
                if (numberOfRatings > currentLargestRatingCount) {
                    selectedFileName = ratingFileNames[i];
                    currentLargestRatingCount = numberOfRatings;
                }
            }

            // Normalize Theoretical Times against Real Run Time
            double realRunTime = runTimePerformanceMap.get(recommendationEngineHarnessTestDriver.getRatingFileNameToFullInputFileNameMap().get(selectedFileName));
            double theoreticalTrend = ratingFileAnalysisMap.get(selectedFileName + TREND_KEY);
            double normalizationFactor = theoreticalTrend / realRunTime;

            return normalizationFactor;
        }


        // Calculates Theoretical Run Time (in seconds) by scaling down algorithm O(n^2*m)
        private void setTheoreticalRunTimePerformanceMap(double normalizationFactor) {
            theoreticalRunTimePerformanceMap = new HashMap<>();
            for (String ratingFileName : ratingFileNames) {
                double theoreticalTrend = ratingFileAnalysisMap.get(ratingFileName + TREND_KEY);
                double normalizedTheoreticalRunTime = theoreticalTrend / normalizationFactor;
                theoreticalRunTimePerformanceMap.put(ratingFileName, normalizedTheoreticalRunTime);
            }
        }

    }

}