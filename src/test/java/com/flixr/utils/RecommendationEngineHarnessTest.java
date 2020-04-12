package com.flixr.utils;

import com.flixr.exceptions.EngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static com.flixr.configuration.ApplicationConstants.REC_ENGINE_THREADS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to running the RecommendationEngine in Stand Alone mode
 */
class RecommendationEngineHarnessTest {

    private RecommendationEngineHarness recommendationEngineHarness;
    private RecommendationEngineHarnessTestOracle recEngineHarnessTestOracle;

    // Create a RecommendationEngineHarness & Oracle for Standalone testing
    @BeforeEach
    public void initEach() {
        recEngineHarnessTestOracle = new RecommendationEngineHarnessTestOracle();
        recommendationEngineHarness = new RecommendationEngineHarness();
    }

    /**
     * TT: Unit Test # 1
     * Ensure that RecommendationEngine Runs & Generates a Valid Correlation Matrix
     *
     * Heuristic Based Approach:
     * 1) Spot check that Matrix diagonal only has zeros
     * 2) Spot check that (i,j) and (j,i) have the same magnitude but are negatives of each other
     *
     */
    @Test
    void testRunOfRecommendationEngineHarness() {

        int numberOfMatrixDiagonalTests = 10;
        int numberOfMatrixSymmetryTests = 100;

        try {
            // Call Engine Harness to generates input objects
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
     * TT: Unit Test # 2
     * Run RecommendationEngineHarness to assess run time performance
     */
    // TODO



    // -----------------------------------------------------------------------------------------------------------------

    // Test Oracle
    private class RecommendationEngineHarnessTestOracle {

        // I/O paths for RecommendationEngineHarness
        private String projectPath;
        private String[] inputFiles;
        private String outputFilePrefix;

        // Instance Variables
        private int numberOfRandomTests;
        private double[][] correlationMatrix;

        public RecommendationEngineHarnessTestOracle() {

            // Set all Oracle Variables here:
            // .........................................................................................................
            numberOfRandomTests = 10;

            // List of CSV test files
            String[] ratingFileNames = new String[] {
                "ml-ratings-u5", // 5 users
                "ml-ratings-u10", // 10 users
                "ml-ratings-u25", // 25 users
                "ml-ratings-u50", // 50 users
                "ml-ratings-u100", // 100 users
                "ml-ratings-u250", // 250 users
                "ml-ratings-u500" // 500 users
            };

            // Input CSV File Name, with format: (UserId, MovieId, Rating)
            String inputFilePath = "/src/test/resources/ml-models/inputs/";
            String outputFilePath = "/src/test/resources/ml-models/outputs/";
            // .........................................................................................................


            // Creates I/O paths
            projectPath = System.getProperty("user.dir");
            inputFiles = new String[ratingFileNames.length];
            for (int i=0; i < inputFiles.length; i++) {
                inputFiles[i] = inputFilePath + ratingFileNames[i] + ".csv";
            }
            outputFilePrefix = outputFilePath + "model";

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
    }









}