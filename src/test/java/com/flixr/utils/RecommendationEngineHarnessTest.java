package com.flixr.utils;

import com.flixr.exceptions.EngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static com.flixr.configuration.ApplicationConstants.REC_ENGINE_THREADS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to running the RecommendationEngine as a Stand Alone Sub-System
 */
class RecommendationEngineHarnessTest {

    private RecommendationEngineHarness recommendationEngineHarness;
    private RecommendationEngineHarnessTestOracle recEngineHarnessTestOracle;

    // Create a new Oracle for Standalone testing
    @BeforeEach
    public void initEach() {
        recEngineHarnessTestOracle = new RecommendationEngineHarnessTestOracle();
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
            // Call Engine Harness to generates input objects
            recommendationEngineHarness = new RecommendationEngineHarness();
            recommendationEngineHarness.readInputFile(recEngineHarnessTestOracle.getProjectPath() + recEngineHarnessTestOracle.getDefaultInputFile());

            // Train Model & Generate Correlation Matrix
            long startTime = System.currentTimeMillis();
            recommendationEngineHarness.trainModel(recEngineHarnessTestOracle.getProjectPath() + recEngineHarnessTestOracle.getOutputFilePrefix());
            long endTime = System.currentTimeMillis();
            System.out.println("\nTotal Run Time: " + (endTime - startTime)/1000.0 + " seconds.");

            // Load Trained Model (i.e. Correlation Matrix)
            recEngineHarnessTestOracle.loadCorrelationMatrix();

            // Step 1 - Test N number of random positions along matrix diagonal
            System.out.println("Confirming Matrix Diagonals...");
            for (int i = 0; i < numberOfMatrixDiagonalTests; i++) {
                double matrixDiagonalValue = recEngineHarnessTestOracle.getRandomMatrixDiagonalValue();
                assertEquals(0.0, matrixDiagonalValue, "Correlation Matrix Diagonal values must be 0.0!");
                System.out.println("Successful Test " + (i+1) + " of " + (numberOfMatrixDiagonalTests + 1));
            }

            // Step 2 - Test N number of random mirrored positions (i,j) = -(j,i)
            System.out.println("\nConfirming Matrix Symmetry...");
            for (int i = 0; i < numberOfMatrixSymmetryTests; i++) {
                int pos_i = recEngineHarnessTestOracle.getRandomMatrixIndex();
                int pos_j = recEngineHarnessTestOracle.getRandomMatrixIndex();
                double matrixValue_i_j = recEngineHarnessTestOracle.getMatrixValue(pos_i, pos_j);
                assertEquals(matrixValue_i_j, recEngineHarnessTestOracle.getMirroredMatrixValue(pos_i, pos_j), "Correlation Matrix must be symmetrical for (" + pos_i + ", " + pos_j + ") and (" + pos_j + ", " + pos_i + ")");
                System.out.println("Successful Test " + (i+1) + " of " + (numberOfMatrixSymmetryTests + 1) + "... Matching Value = " + matrixValue_i_j);
            }

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

            // Iterate over all input files from Test Oracle
            for (String fileName : recEngineHarnessTestOracle.getInputFiles()) {
                // Call Engine Harness to generates input objects
                recommendationEngineHarness = new RecommendationEngineHarness();
                recommendationEngineHarness.readInputFile(recEngineHarnessTestOracle.getProjectPath() + fileName);

                // Train Model & Generate Correlation Matrix
                long startTime = System.currentTimeMillis();
                recommendationEngineHarness.trainModel(recEngineHarnessTestOracle.getProjectPath() + recEngineHarnessTestOracle.getOutputFilePrefix());
                long endTime = System.currentTimeMillis();
                double runTimeInSeconds = (endTime - startTime)/1000.0;
                System.out.println("\nCurrent Iteration Run Time: " + runTimeInSeconds + " seconds.");

                // Record performance into Oracle
                recEngineHarnessTestOracle.addRunTimePerformanceEntry(fileName, runTimeInSeconds);
            }

            // Calculate Input File Properties
            recEngineHarnessTestOracle.analyzeInputFiles();

            // Display Performance Results
            recEngineHarnessTestOracle.printRunTimePerformanceResults();

        } catch (Exception e) {
            fail("Unable to complete run! Error was thrown: " + e.getMessage());
        }
    }


    // -----------------------------------------------------------------------------------------------------------------

    // Test Oracle
    private class RecommendationEngineHarnessTestOracle {

        // Keys for HashMap Analysis
        String MOVIES_KEY = "<>NUM_MOVIES";
        String USERS_KEY = "<>NUM_USERS";
        String RATINGS_KEY = "<>" + "NUM_RATINGS";
        String TREND_KEY = "<>" + "N2xM";

        // I/O paths for RecommendationEngineHarness
        private String projectPath;
        private String[] ratingFileNames;
        private String[] inputFiles;
        private String outputFilePrefix;
        private String performanceFilePath;

        // Instance Variables
        private int numberOfRandomTests;
        private double[][] correlationMatrix;
        private HashMap<String,String> ratingFileNameToFullInputFileNameMap = new HashMap<>();
        private HashMap<String,Double> runTimePerformanceMap; // key = fullFileName, value = real run time
        private HashMap<String,Double> ratingFileAnalysisMap; // key = fullFileName<>ATTRIBUTE
        private HashMap<String,Double> theoreticalRunTimePerformanceMap; // key = fullFileName, value = theoretical run time (normalized)

        public RecommendationEngineHarnessTestOracle() {

            // Set all Oracle Variables here:
            // .........................................................................................................
            numberOfRandomTests = 10;

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
                    bufferedReader = new BufferedReader(new FileReader(this.getProjectPath() + this.getOutputFilePrefix() + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv"));
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
                    System.out.println("Unable to read input file: \n" + this.getProjectPath() + this.getOutputFilePrefix() + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv");
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
                    bufferedReader = new BufferedReader(new FileReader(this.getProjectPath() + this.getOutputFilePrefix() + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv"));
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
                    System.out.println("Unable to read input file: \n" + this.getProjectPath() + this.getOutputFilePrefix() + "-" + fileNumber + "-of-" + REC_ENGINE_THREADS + ".csv");
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
                    bufferedReader = new BufferedReader(new FileReader(this.getProjectPath() + ratingFileNameToFullInputFileNameMap.get(ratingFileName)));
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
                    System.out.println("Unable to read input file: \n" + ratingFileNameToFullInputFileNameMap.get(ratingFileName));
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

        public String getDefaultInputFile() {
            return inputFiles[1]; // default is 10 users
        }

        public String[] getInputFiles() {
            return inputFiles;
        }

        public String getOutputFilePrefix() {
            return outputFilePrefix;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public int getNumberOfRandomTests() {
            return numberOfRandomTests;
        }

        public double[][] getCorrelationMatrix() {
            return correlationMatrix;
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
                        runTimePerformanceMap.get(ratingFileNameToFullInputFileNameMap.get(ratingFileName)) + ", " +
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
            double realRunTime = runTimePerformanceMap.get(ratingFileNameToFullInputFileNameMap.get(selectedFileName));
            double theoreticalTrend = ratingFileAnalysisMap.get(selectedFileName + TREND_KEY);
            double normalizationFactor = theoreticalTrend / realRunTime;

            return normalizationFactor;
        }

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