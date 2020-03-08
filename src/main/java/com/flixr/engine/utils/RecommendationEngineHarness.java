package com.flixr.engine.utils;

import com.flixr.engine.RecommendationEngine;
import com.flixr.exceptions.EngineException;
import com.flixr.beans.UserSubmission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    private void trainModel(String outputFilePath) {
        RecommendationEngine recommendationEngine = new RecommendationEngine(sortedListOfMovieIds);
        try {
            recommendationEngine.trainModel(sortedListOfUserSubmissions, outputFilePath);
        } catch (EngineException e) {
            System.out.println("Error during training of the RecommendationEngine!");
            System.out.println(e.getEngineMessage());
        }
    }


    // Trains the model (and saves to DB)
    private void trainModel() {
        RecommendationEngine recommendationEngine = new RecommendationEngine(sortedListOfMovieIds);
        try {
            recommendationEngine.trainModel(sortedListOfUserSubmissions);
        } catch (EngineException e) {
            System.out.println("Error during training of the RecommendationEngine!");
            System.out.println(e.getEngineMessage());
        }
    }

    // Creates an RecommendationEngineHarness for Standalone model training
    public static void main(String[] args) {

        // Select Training File Name & CSV or DB Test
        String ratingFileName = "ml-extra-small-ratings";
        boolean saveToDB = true;

        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/test_data/" + ratingFileName + ".csv";
        String outputFile = "/test_data/outputs/models/model-" + ratingFileName + ".csv";

        // Create Engine Harness (which generates input objects)
        RecommendationEngineHarness recommendationEngineHarness = new RecommendationEngineHarness();
        recommendationEngineHarness.readInputFile(pathName + inputFile);

        // Run a training test
        long startTime = System.currentTimeMillis();
        // Database vs CSV test
        if (saveToDB) {
            // Trains the Recommendation Engine (and saves to DB)
            recommendationEngineHarness.trainModel();
        } else {
            // Trains the Recommendation Engine (and saves to CSV)
            recommendationEngineHarness.trainModel(pathName + outputFile);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal Run Time: " + (endTime - startTime) + " ms.");

    }

}
