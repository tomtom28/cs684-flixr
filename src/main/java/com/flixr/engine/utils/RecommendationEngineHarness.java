package com.flixr.engine.utils;

import com.flixr.engine.RecommendationEngine;
import com.flixr.engine.exceptions.EngineException;
import com.flixr.engine.io.RecEngineInput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * Used to launch a Stand Alone version of the Recommendation Engine
 * Purpose is for backend testing / development
 *
 */
public class RecommendationEngineHarness {

    private List<RecEngineInput> recEngineInputs;
    private TreeSet<Integer> sortedListOfMovieIds;

    public RecommendationEngineHarness() {
        this.recEngineInputs = new ArrayList<>();
        this.sortedListOfMovieIds = new TreeSet<>();
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

                // Add to list of inputs
                RecEngineInput recEngineInput = new RecEngineInput(userId, movieId, rating);
                recEngineInputs.add(recEngineInput);

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

    // Trains the model (and saves to CSV)
    private void trainModel(String outputFilePath) {
        RecommendationEngine recommendationEngine = new RecommendationEngine(sortedListOfMovieIds);
        try {
            recommendationEngine.trainModel(recEngineInputs, outputFilePath);
        } catch (EngineException e) {
            System.out.println("Error during training of the RecommendationEngine!");
            System.out.println(e.getEngineMessage());
        }

    }

    // Creates an RecommendationEngineHarness for Standalone model training
    public static void main(String[] args) {

        // Select Training File Name
        String ratingFileName = "ml-small-ratings";

        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/test_data/" + ratingFileName + ".csv";
        String outputFile = "/test_data/outputs/models/model-" + ratingFileName + ".csv";

        // Create Engine Harness (which generates input objects)
        RecommendationEngineHarness recommendationEngineHarness = new RecommendationEngineHarness();
        recommendationEngineHarness.readInputFile(pathName + inputFile);

        // Trains the Recommendation Engine (and saves to CSV)
        long startTime = System.currentTimeMillis();
        recommendationEngineHarness.trainModel(pathName + outputFile);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal Run Time: " + (endTime - startTime) + " ms.");

    }

}
