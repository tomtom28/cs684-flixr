package com.flixr.engine;

import com.flixr.dao.EngineDAO;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.beans.UserSubmission;
import com.flixr.interfaces.IRecommendationEngineDAO;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

/**
 * @author Thomas Thompson
 *
 * Recommendation Engine:
 * Used to generate correlations between movies based on collaborative filtering
 * Uses the Slope One algorithm
 *
 * All code is original, however the following source was referenced for Pseudo-Code / Design Decisions:
 * http://girlincomputerscience.blogspot.com/search/label/Recommender%20Systems
 *
 * The output of this model gives the Average Rating Difference between two different Movies
 */

public class RecommendationEngine {

    // Represents a 2D matrix
    private double[][] matrixOfMovieToMovieCorrelation; // Trained Model: (MovieId, MovieId) -> Rating Factor of IPrediction
    private double[][] matrixOfMovieToMovieRatingDifferenceSums; // (MovieId, MovieId) -> Sum of all Movie to Movie Rating Differences among Users
    private int[][] matrixOfMovieToMovieRatingFrequency; // (MovieId, MovieId) -> Number of Users who Rated Both Movies in their submission
    private int movieCount; // size of NxN matrix

    // Maps all MovieIds to a Matrix Index:
    private HashMap<Integer, Integer> matrixIndexToMovieId; // Index -> MovieId

    // Maps all UserIds to all movies they rated: UserID -> (MovieId, Rating)
    private Map<Integer, UserSubmission> userSubmissions;

    /**
     * Creates a RecommendationEngine instance
     * @param sortedListOfMovieIds    List of Unique MovieIds
     */
    public RecommendationEngine(TreeSet<Integer> sortedListOfMovieIds) {

        // Create 2D Matrices
        movieCount = sortedListOfMovieIds.size();
        matrixOfMovieToMovieCorrelation = new double[movieCount][movieCount];
        matrixOfMovieToMovieRatingDifferenceSums = new double[movieCount][movieCount];
        matrixOfMovieToMovieRatingFrequency = new int[movieCount][movieCount];

        // Map MovieId to Matrix Index
        matrixIndexToMovieId = new HashMap<>();
        int index = 0;
        for (Integer movieId : sortedListOfMovieIds) {
            matrixIndexToMovieId.put(index,movieId);
            index++;
        }
    }


    /**
     * Runs an implementation of the SlopeOne algorithm to determine the rating correlation between movies
     * Outputs are stored in a Database using the EngineDAO
     *
     * @param userSubmissions  List of User Submissions (UserId, MovieId, Rating); NOTE: the list must be sorted by UserId
     */
    public void trainModel(Map<Integer, UserSubmission> userSubmissions) throws EngineException {

        // Store list of UserSubmissions
        this.userSubmissions = userSubmissions;

        // Generate Correlations Between (MovieId to MovieId) and (Rating)
        generateCorrelationMatrix();

        // Save Correlation Matrix to Database
        saveModelToDB();

    }


    /**
     * Runs an implementation of the SlopeOne algorithm to determine the rating correlation between movies
     * Outputs are stored in a CSV file at the file patch specified
     *
     * @param userSubmissions  List of RecEngineInput Objects (UserId, MovieId, Rating); NOTE: the list must be sorted by UserId
     */
    public void trainModel(Map<Integer, UserSubmission> userSubmissions, String fullOutputFilePath) throws EngineException {

        // Store list of UserSubmissions
        this.userSubmissions = userSubmissions;

        // Generate Correlations Between (MovieId to MovieId) and (Rating)
        generateCorrelationMatrix();

        // Save Correlation Matrix to CSV file
        saveModelToCSV(fullOutputFilePath);
    }


    // Saves the correlation matrix to a CSV files
    // This is used in StandAlone Mode
    private void saveModelToCSV(String fullOutputFilePath) throws EngineException {

        // Compute Average Rating Differences and Save to CSV
        PrintWriter writer = null;
        try {
            // Make Write & Print Header Row
            writer = new PrintWriter(fullOutputFilePath, "UTF-8");
            writer.println("MovieID_i,MovieId_j,correlation");

            // Iterate over all movies to get (Sum of Rating Difference) / (Count of Ratings)
            for (int i = 0; i < movieCount; i++) {
                for (int j = 0; j < movieCount; j++) {

                    // Only average movies that were rated
                    if (matrixOfMovieToMovieRatingFrequency[i][j] > 0) {
                        matrixOfMovieToMovieCorrelation[i][j] = matrixOfMovieToMovieRatingDifferenceSums[i][j] / matrixOfMovieToMovieRatingFrequency[i][j];
                    }

                    // Write to File
                    writer.println(matrixIndexToMovieId.get(i) + "," + matrixIndexToMovieId.get(j) + "," + matrixOfMovieToMovieCorrelation[i][j]);

                }

                // Print progress
                System.out.println("Saving Correlation Matrix: Completed Row " + (i+1) + " of " + movieCount);
            }

        } catch (Exception e) {
            EngineException ee = new EngineException(e);
            ee.setEngineMessage("Unable to Save Trained Model.");
            throw ee;

        } finally {
            // Closes CSV output writer
            if (writer != null) writer.close();
        }
    }


    // Saves the correlation matrix to a Database
    private void saveModelToDB() throws EngineException {

        // Compute Average Rating Differences and Save to Database
        try {

            // Initialize Database Connection
            // Due to the sheer # of entries, using the DAO was too slow
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO RecEngineModel VALUES (?,?,?)");

            // Iterate over all movies to get (Sum of Rating Difference) / (Count of Ratings)
            for (int i = 0; i < movieCount; i++) {
                for (int j = 0; j < movieCount; j++) {

                    // Only average movies that were rated
                    if (matrixOfMovieToMovieRatingFrequency[i][j] > 0) {
                        matrixOfMovieToMovieCorrelation[i][j] = matrixOfMovieToMovieRatingDifferenceSums[i][j] / matrixOfMovieToMovieRatingFrequency[i][j];
                    }

                    // Add current matrix index entry for this row to Database Query
                    int movieId_i = matrixIndexToMovieId.get(i);
                    int movieId_j = matrixIndexToMovieId.get(j);
                    double avgDifference = matrixOfMovieToMovieCorrelation[i][j];
                    stmt.setInt(1, movieId_i);
                    stmt.setInt(2, movieId_j);
                    stmt.setDouble(3, avgDifference);
                    stmt.addBatch();
                }

                // Print progress
                System.out.println("Generating Correlation Matrix Query: Completed Row " + (i+1) + " of " + movieCount);
            }

            // Run the Query & Close Connection
            long startTime = System.currentTimeMillis();
            System.out.println("Running Correlation Matrix Query...");
            stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            System.out.println("Correlation Matrix Saved to Database...");
            System.out.println("Query Time: " + (endTime - startTime) + " ms.");
            conn.close();

        } catch (SQLException e) {
            EngineException ee = new EngineException(e);
            ee.setEngineMessage("Unable to Save Trained Model.");
            ee.getStackTrace();
            throw ee;
        }
    }


    // Generates the Correlation Matrix between Movie to Movie Ratings
    // Also tracks movie rating frequency
    private void generateCorrelationMatrix() {

        // Iterate over every MovieIndex
        for (int i = 0; i < movieCount; i++) {

            // Iterate over every other MovieIndex
            for (int j = 0; j < movieCount; j++) {
                if (i!=j) {

                    // Convert to MovieIndices to MovieIds
                    int movieId_i = matrixIndexToMovieId.get(i);
                    int movieId_j = matrixIndexToMovieId.get(j);

                    // Iterate over every UserId
                    for (int userId : userSubmissions.keySet()) {
                        // Add to matrices if the user rated both MovieIds
                        UserSubmission userSubmission = userSubmissions.get(userId);
                        if (userSubmission.getMoviesViewed().contains(movieId_i) && userSubmission.getMoviesViewed().contains(movieId_j)) {
                            // Add Rating Difference to a running Sum of Differences
                            matrixOfMovieToMovieRatingDifferenceSums[i][j] +=  userSubmission.getMovieRating(movieId_i) - userSubmission.getMovieRating(movieId_j);
                            // Increment Rating Count
                            matrixOfMovieToMovieRatingFrequency[i][j] += 1;
                        }
                    }

                }
            }
            // Print progress
            System.out.println("Correlation Matrix Computation: Completed Row " + (i+1) + " of " + movieCount);
        }

    }

}
