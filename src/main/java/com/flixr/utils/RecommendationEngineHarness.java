package com.flixr.utils;

import com.flixr.beans.UserSubmission;
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
 * Used to launch a Stand Alone version of the Recommendation Engine
 * Purpose is for backend testing / development
 *
 */
public class RecommendationEngineHarness {

    private TreeSet<Integer> sortedListOfMovieIds;
    private TreeMap<Integer, UserSubmission> sortedListOfUserSubmissions;

    public RecommendationEngineHarness() {
        this.sortedListOfMovieIds = new TreeSet<>();
        this.sortedListOfUserSubmissions = new TreeMap<>();
    }

    // Reads file, assuming it is in CSV format
    private void readInputFile(String inputFilePath) {
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
                sortedListOfMovieIds.add(movieId);
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Unable to read input file: \n" + inputFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        }
    }


    // Maps all UserIds to submitted (Movie, Rating) pairs
    // NOTE: recEngineInputs must be sorted by UserId
    private void processRating(int userId, int movieId, double rating) {

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

    // Trains the model (and saves to CSV)
    private void trainModel(String outputFilePrefix) {
        // Split Up List of MovieIds
        int[] splitIndxs = getMatrixSplitPoints(this.sortedListOfMovieIds);
        List<Integer> listOfDistinctMovieIds = new ArrayList<>(this.sortedListOfMovieIds);

        // Run MultiThreaded
        ExecutorService executor = Executors.newFixedThreadPool(REC_ENGINE_THREADS);
        for (int i = 1; i <= REC_ENGINE_THREADS; i++) {
            // Create a subset of the movies for Rec Engine Thread
            TreeSet<Integer> subsetOfDistinctMovieIds = new TreeSet<>( listOfDistinctMovieIds.subList(splitIndxs[i-1], splitIndxs[i]) ) ;

            // Create New Output File for given Thread
            String currentOutputFilePath = outputFilePrefix + "-" + i +"-of-" + REC_ENGINE_THREADS + ".csv";

            // Instantiate the given Engine Thread
            RecEngineThread recEngineThread = new RecEngineThread(i, subsetOfDistinctMovieIds, this.sortedListOfUserSubmissions, currentOutputFilePath);

            // Run a service to compute a subset of the matrix, using the given Engine Thread
            executor.execute(recEngineThread);
        }

        // Wait for threads to complete
        executor.shutdown();
        while (!executor.isTerminated()) {}
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

    // Creates an RecommendationEngineHarness for Standalone model training
    public static void main(String[] args) {

        // Select Training File Name & CSV or DB Test
        String ratingFileName = "ml-small-ratings";

        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/test_data/" + ratingFileName + ".csv";
        String outputFilePrefix = "/test_data/outputs/models/model-" + ratingFileName;

        // Create Engine Harness (which generates input objects)
        RecommendationEngineHarness recommendationEngineHarness = new RecommendationEngineHarness();
        recommendationEngineHarness.readInputFile(pathName + inputFile);

        // Train Model using Multi Threading for faster run time
        long startTime = System.currentTimeMillis();
        recommendationEngineHarness.trainModel(pathName + outputFilePrefix);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal Run Time: " + (endTime - startTime) + " ms.");

    }

}
