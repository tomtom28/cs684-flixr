package com.flixr.threads;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Thomas Thompson
 * Faster Multi Threaded CSV reader for PredictionDAO
 */

public class ReadModelCsvThread extends Thread {

    private int threadNumber;
    private String matrixCsvFilePath;
    private HashMap<Integer, Integer> movieIdToMatrixIndex;
    private double[][] correlationMatrix;


    public ReadModelCsvThread(int threadNumber, String matrixCsvFilePath,
                              HashMap<Integer, Integer> movieIdToMatrixIndex, double[][] correlationMatrix) {

        this.threadNumber = threadNumber;
        this.correlationMatrix = correlationMatrix;
        this.matrixCsvFilePath = matrixCsvFilePath;
        this.movieIdToMatrixIndex = movieIdToMatrixIndex;

    }

    @Override
    public void run() throws RuntimeException {

        System.out.println("Thread-" + threadNumber + " Reading Matrix CSV file: " + matrixCsvFilePath);

        // Reads file, assuming it is in CSV format
        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(matrixCsvFilePath));
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
            System.out.println("Unable to read input file: \n" + matrixCsvFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        } catch (NullPointerException e ) {
            e.printStackTrace();
            System.out.println("Unable to parse the following line: \n" + line + "\n" +
                    "Likely issue: The movieIdToMatrixIndex may be missing a movieId. \n" +
                    "Possible fix: Ensure that the model.csv and ratings.csv are aligned.");
        }

        System.out.println("Thread-" + threadNumber + " File Reader is completed.");
    }

}
