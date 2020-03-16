package com.flixr.threads;

import java.sql.*;
import java.util.HashMap;
import java.util.Set;

import static com.flixr.configuration.ApplicationConstants.*;

/**
 * @author Thomas Thompson
 *
 * This thread is used by the PredictionDAO to run multiple MySQL queries when retrieving the movie Correlation Matrix from the database
 */
public class PredictionMatrixThread extends Thread {

    private int threadNumber;
    private Set<Integer> distinctMovieIds;
    private double[][] correlationMatrix;
    private HashMap<Integer,Integer> movieIdToMatrixIndex;

    public PredictionMatrixThread(int threadNumber, Set<Integer> distinctMovieIds,
                                  double[][] correlationMatrix, HashMap<Integer,Integer> movieIdToMatrixIndex) {
        this.threadNumber = threadNumber;
        this.distinctMovieIds = distinctMovieIds;
        this.correlationMatrix = correlationMatrix;
        this.movieIdToMatrixIndex = movieIdToMatrixIndex;
    }

    @Override
    public void run() throws RuntimeException {
        // Query Database to Append to Correlation Matrix
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recenginemodel WHERE movieIdi = ?");

            // Counter for logging
            int count = 0;

            // Iterate over all stored Rows in Matrix
            for (int movieIdi : distinctMovieIds) {

                stmt.setInt(1, movieIdi);
                ResultSet resultSet = stmt.executeQuery();

                // Iterate Over all DB Entries
                while (resultSet.next()) {

                    // Assumes format: (MovieId_i,MovieId_j,Rating)
                    int movieIdMatrix_i = resultSet.getInt("MovieIdi");
                    int movieIdMatrix_j = resultSet.getInt("MovieIdj");
                    double avgRatingDifference = resultSet.getDouble("AvgDifference");;

                    // Convert MovieId to Matrix Index
                    int i = movieIdToMatrixIndex.get(movieIdMatrix_i);
                    int j = movieIdToMatrixIndex.get(movieIdMatrix_j);

                    // Add to internal matrix
                    correlationMatrix[i][j] = avgRatingDifference;
                }
                count++;
                System.out.println("Thread-" + threadNumber + " Loaded Matrix Row: " + count + " of " + distinctMovieIds.size());
            }

            // Completed Loading Matrix, close connection
            System.out.println("Thread-" + threadNumber + " Completed Successfully.");
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
