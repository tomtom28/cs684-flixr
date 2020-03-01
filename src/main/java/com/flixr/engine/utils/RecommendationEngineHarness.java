package com.flixr.engine.utils;

import com.flixr.engine.RecommendationEngine;

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
public class EngineTrainer {

    private List<EngineInput> engineInputs;
    private TreeSet<Integer> sortedListOfMovieIds;

    public EngineTrainer() {
        this.engineInputs = new ArrayList<>();
        this.sortedListOfMovieIds = new TreeSet<>();
    }

    // Reads file, assuming it is in CSV format
    private void readInputFile(String inputFilePath) {
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
            bufferedReader.readLine(); // skips header row
            while ( (line = bufferedReader.readLine()) != null ) {

                // Assumes format: (UserId,MovieId,Rating)
                String[] input = line.split(",");
                int userId = Integer.parseInt(input[0]);
                int movieId = Integer.parseInt(input[1]);
                double rating = Double.parseDouble(input[2]);

                // Add to list of inputs
                EngineInput engineInput = new EngineInput(userId, movieId, rating);
                engineInputs.add(engineInput);

                // Add to list of (unique) sorted MovieIds
                sortedListOfMovieIds.add(movieId);
            }
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
            recommendationEngine.trainModel(engineInputs, outputFilePath);
        } catch (RecommendationEngineException e) {
            System.out.println("Error during training of the RecommendationEngine!");
            System.out.println(e.getEngineMessage());
        }

    }

    // Creates an EngineTrainer for Standalone model training
    public static void main(String[] args) {

        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/test_data/ml-latest-extra-small-ratings.csv";
        String outputFile = "/test_data/engine-output.csv";

        // Create Engine Harness (which generates input objects)
        EngineTrainer engineTrainer = new EngineTrainer();
        engineTrainer.readInputFile(pathName + inputFile);

        // Trains the Recommendation Engine (and saves to CSV)
        long startTime = System.currentTimeMillis();
        engineTrainer.trainModel(pathName + outputFile);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal Run Time: " + (endTime - startTime) + " ms.");

    }

}
