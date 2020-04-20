package com.flixr.utils.helpers.RecommendationEngine;

import com.flixr.beans.UserSubmission;
import com.flixr.exceptions.EngineException;
import com.flixr.threads.RecEngineThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flixr.configuration.ApplicationConstants.REC_ENGINE_THREADS;

/**
 * @author Thomas Thompson
 *
 * Test Driver for Recommendation Engine
 */
public class RecommendationEngineHarnessTestDriver {

    // I/O paths
    private String projectPath;
    private String[] ratingFileNames;
    private String[] inputFiles;
    private String defaultInputFile;
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

        // Default file for light training (uses ImdbId, not MovieId)
        String defaultInputFileName = "ml-ratings-imdb-u10";

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
        defaultInputFile = inputFilePath + defaultInputFileName + ".csv";

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
        return projectPath + defaultInputFile;
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


    public String getPerformanceFilePath() {
        return performanceFilePath;
    }

}
