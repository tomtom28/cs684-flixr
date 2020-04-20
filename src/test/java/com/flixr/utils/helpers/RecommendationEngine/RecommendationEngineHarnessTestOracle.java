package com.flixr.utils.helpers.RecommendationEngine;

import com.flixr.exceptions.EngineException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Thompson
 *
 * Test Oracle for Recommendation Engine
 */
public class RecommendationEngineHarnessTestOracle {

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
    private RecommendationEngineHarnessTestDriver recommendationEngineHarnessTestDriver;
    private HashMap<String,Double> runTimePerformanceMap; // key = fullFileName, value = real run time
    private HashMap<String,Double> ratingFileAnalysisMap; // key = fullFileName<>ATTRIBUTE
    private HashMap<String,Double> theoreticalRunTimePerformanceMap; // key = fullFileName, value = theoretical run time (normalized)

    public RecommendationEngineHarnessTestOracle() {}


    public void intialize(RecommendationEngineHarnessTestDriver recommendationEngineHarnessTestDriver) {
        this.recommendationEngineHarnessTestDriver = recommendationEngineHarnessTestDriver;
        this.projectPath = recommendationEngineHarnessTestDriver.getProjectPath();
        this.performanceFilePath = recommendationEngineHarnessTestDriver.getPerformanceFilePath();
        this.ratingFileNames = recommendationEngineHarnessTestDriver.getRatingFileNames();
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
            System.out.println("Tested " + (i+1) + " of " + (numberOfMatrixSymmetryTests) + "... Matching Value = " + matrixValue_i_j);
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