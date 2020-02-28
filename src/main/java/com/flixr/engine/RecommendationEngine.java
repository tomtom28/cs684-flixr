package com.flixr.engine;

import com.flixr.engine.utils.EngineInput;
import com.flixr.engine.utils.RecommendationEngineException;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * Recommendation Engine:
 * Used to predict movies based on collaborative filtering
 * Uses Slope One algorithm
 *
 */

public class RecommendationEngine {

    // Represents a 2D matrix
    private double[][] matrixOfMovieToMovieCorrelation; // Trained Model: (MovieId, MovieId) -> Rating Factor of Prediction
    private double[][] matrixOfMovieToMovieRatingDifferenceSums; // (MovieId, MovieId) -> Sum of all Movie to Movie Rating Differences among Users
    private int[][] matrixOfMovieToMovieRatingFrequency; // (MovieId, MovieId) -> Number of Users who Rated Both Movies in their submission
    private int movieCount; // size of NxN matrix

    // Maps all MovieIds to a Matrix Index:
    private HashMap<Integer, Integer> movieIdToMatrixIndex; // MovieId -> Index
    private HashMap<Integer, Integer> matrixIndexToMovieId; // Index -> MovieId

    // Maps all UserIds to all movies they rated: UserID -> (MovieId, Rating)
    private HashMap<Integer, HashMap<Integer, Double>> userSubmissions;


    /**
     * Creates a RecommendationEngine instance
     * @param sortedListOfMovieIds    Sorted List of Unique MovieIds
     */
    public RecommendationEngine(TreeSet<Integer> sortedListOfMovieIds) {

        // Create 2D Matrices
        movieCount = sortedListOfMovieIds.size();
        matrixOfMovieToMovieCorrelation = new double[movieCount][movieCount];
        matrixOfMovieToMovieRatingDifferenceSums = new double[movieCount][movieCount];
        matrixOfMovieToMovieRatingFrequency = new int[movieCount][movieCount];

        // Map MovieId to Matrix Index, and vice versa
        movieIdToMatrixIndex = new HashMap<>();
        matrixIndexToMovieId = new HashMap<>();
        int index = 0;
        for (Integer movieId : sortedListOfMovieIds) {
            movieIdToMatrixIndex.put(movieId, index);
            matrixIndexToMovieId.put(index,movieId);
            index++;
        }
    }

    /**
     * Runs an implementation of the SlopeOne algorithm to determine the rating correlation between movies
     * Outputs are stored in a database to avoid excessive memory consumption
     *
     * @param engineInputs  List of EngineInput Objects (UserId, MovieId, Rating); the list must be sorted by UserId
     */
    public void trainModel(List<EngineInput> engineInputs) throws RecommendationEngineException {
        try {
            // Match UserIds to (MovieId, Rating) submissions
            setUserSubmissions(engineInputs);

            // Generate Correlations Between (MovieId to MovieId) and (Rating)
            generateCorrelationMatrix();


        } catch (Exception e) {
            throw new RecommendationEngineException(e);
        }
    }


    // Maps all UserIds to submitted (Movie, Rating) pairs
    // NOTE: engineInputs must be sorted by UserId
    private void setUserSubmissions(List<EngineInput> engineInputs) throws Exception {
        userSubmissions = new HashMap<>();

        // Iterate over EngineInputs and map UserId to (Movie, Rating)
        int userId = -1;
        HashMap<Integer, Double> currentUserSubmission = null; // stores current HashMap reference (for speed)
        for (EngineInput engineInput : engineInputs) {

            // Given a new UserId, create a new submission entry
            if (userId != engineInput.getUserId()) {
                userId = engineInput.getUserId();
                userSubmissions.put(userId, new HashMap<>());
                currentUserSubmission = userSubmissions.get(userId);
            }

            // Append to the current submission entry
            currentUserSubmission.put(engineInput.getMovieId(), engineInput.getRating());

        }

    }


    private void generateCorrelationMatrix() {

        // Iterate over every MovieId
        for (int i = 0; i < movieCount; i++) {

            // Iterate over every other MovieId
            for (int j = 0; j < movieCount; j++) {
                if (i!=j) {

                    // Convert to MovieIds
                    int movieId_i = matrixIndexToMovieId.get(i);
                    int movieId_j = matrixIndexToMovieId.get(j);

                    // Iterate over every UserId
                    for (int userId : userSubmissions.keySet()) {
                        // Determine if the user rated both MovieIds
                        HashMap<Integer, Double> userSubmission = userSubmissions.get(userId);
                        if (userSubmission.keySet().contains(movieId_i) && userSubmission.keySet().contains(movieId_j)) {
                            // Add Rating Difference to a running Sum of Differences
                            matrixOfMovieToMovieRatingDifferenceSums[i][j] +=  userSubmission.get(movieId_i) - userSubmission.get(movieId_j);
                            // Increment Rating Count
                            matrixOfMovieToMovieRatingFrequency[i][j] += 1;
                        }
                    }

                }
            }
        }


        // Iterate over all movies to get (Sum of Rating Difference) / (Count of Ratings)
        for (int i = 0; i < movieCount; i++) {
            for (int j = 0; j < movieCount; j++) {
                // Only average movies that were rated
                if (matrixOfMovieToMovieRatingFrequency[i][j] > 0) {
                    matrixOfMovieToMovieCorrelation[i][j] = matrixOfMovieToMovieRatingDifferenceSums[i][j] / matrixOfMovieToMovieRatingFrequency[i][j];
                }
            }
        }

        int DEBUG = 0;

    }


}
