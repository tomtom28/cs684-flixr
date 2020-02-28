package com.flixr.engine.utils;

import com.flixr.engine.RecommendationEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * Used to launch a Stand Alone version of the Recommendation Engine
 * Purpose is for backend testing / development
 *
 */
public class EngineHarness {

    private String inputFilePath;
    private List<EngineInput> engineInputs;
    private TreeSet<Integer> sortedListOfMovieIds;

    public EngineHarness(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        this.engineInputs = new ArrayList<>();
        this.sortedListOfMovieIds = new TreeSet<>();
    }

    // Reads file, assuming it is in CSV format
    private void readInputFile() {
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

    // Trains the model
    private void trainModel() {
        RecommendationEngine recommendationEngine = new RecommendationEngine(sortedListOfMovieIds);
//        RecommendationEngine recommendationEngine = new RecommendationEngine(3952);
        try {
            recommendationEngine.trainModel(engineInputs);
        } catch (RecommendationEngineException e) {
            System.out.println("Error during training of the RecommendationEngine!");
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) {

        // Selects a CSV to parse
        String pathName = System.getProperty("user.dir");
        String fileName = "/test_data/ml-latest-small-ratings.csv";
        EngineHarness engineHarness = new EngineHarness(pathName + fileName);
        engineHarness.readInputFile();

        int DEBUG_1 = 0;

        // Trains the Recommendation Engine
        engineHarness.trainModel();

        int DEBUG_2 = 0;
    }

}
