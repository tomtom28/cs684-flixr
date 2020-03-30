package com.flixr.dao;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.Prediction;
import com.flixr.exceptions.DAOException;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.flixr.configuration.ApplicationConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to the PredictionDAO
 */
class PredictionDAOTest {

    private static double ARBITRARY_PREDICTION_RATING = 4.9;
    private static int NUM_OF_MOVIE_PREDICIONS = 10;

    /**
     * TT: Unit Test # 6
     * This test is meant to ensure the list of Predictions is properly converted into a list of MoviesWithPredictions
     * Note that Predictions are randomly generated.
     */
    @Test
    void testGetPredictedMovies() {

        try {
            int numberOfMoviePredictions = NUM_OF_MOVIE_PREDICIONS;

            // Create Randomly Generated Predictions
            PredictionDAOTestHelper predictionDAOTestHelper = new PredictionDAOTestHelper();
            List<Prediction> predictions = predictionDAOTestHelper.generateRandomPredictions(numberOfMoviePredictions);

            // Generate MoviesWithPredictions using PredictionDAO
            PredictionDAO predictionDAO = new PredictionDAO();
            List<MovieWithPrediction> predictedMovies = predictionDAO.getPredictedMovies(predictions);

            // Check that MovieWithPrediction objects are aligned with database
            List<MovieWithPrediction> expectedPredictedMovies = predictionDAOTestHelper.generateMoviesWithPredictions(predictions);

            // Check that all fields are aligned
            assertTrue(predictionDAOTestHelper.isValidMovieWithPredictions(expectedPredictedMovies, predictedMovies),
                    "List of Movies with Predictions must match!");

        } catch (Exception e) {
            fail("Unable to generate list of MoviesWithPredictions!");
        }

    }

    // -----------------------------------------------------------------------------------------------------------------

    // Helper Methods for this PredictionDAOTest class
    private class PredictionDAOTestHelper {

        /**
         * @param numberOfPredictions   Number of Predictions Needed
         * @return  Random List of Predictions
         */
        private List<Prediction> generateRandomPredictions(int numberOfPredictions) {
            try {
                List<Prediction> listOfRandomPredictions = new ArrayList<>();
                Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                String query =  "SELECT MovieID FROM movies " +
                                "ORDER BY RAND() " +
                                "LIMIT ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, numberOfPredictions);
                ResultSet resultSet = stmt.executeQuery();

                while(resultSet.next()) {
                    int movieId = resultSet.getInt("MovieID"); // Get MovieId
                    double predictedRating = ARBITRARY_PREDICTION_RATING; // Make Arbitrary Prediction
                    listOfRandomPredictions.add(new Prediction(movieId,predictedRating));
                }

                // Close connection and return
                conn.close();
                return listOfRandomPredictions;

            } catch (SQLException e) {
                return null;
            }
        }


        /**
         * @param listOfPredictions List of Predictions
         * @return  List of MoviesWithPredictions (i.e. predictions converts into movie subclass)
         */
        private List<MovieWithPrediction> generateMoviesWithPredictions(List<Prediction> listOfPredictions) {

            // Determine all Movie Ids
            String movieIds = "";
            for (Prediction prediction : listOfPredictions) {
                movieIds += prediction.getMovieId() + ",";
            }
            movieIds = movieIds.substring(0,movieIds.length()-1); // remove trailing comma

            // Generate MoviesWithPredictions
            try {
                List<MovieWithPrediction> listOfMoviesWithPredictions = new ArrayList<>();
                Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                String query =  "SELECT * FROM movies " +
                        "WHERE MovieID IN (" + movieIds + ")";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet resultSet = stmt.executeQuery();

                // Make the MovieWithPrediction & append to list
                while(resultSet.next()) {
                    MovieWithPrediction movieWithPrediction = new MovieWithPrediction();
                    movieWithPrediction.setMovieID(resultSet.getInt("movieID"));
                    movieWithPrediction.setMoviename(resultSet.getString("movieName"));
                    movieWithPrediction.setReleasedate(resultSet.getString("releaseDate"));
                    movieWithPrediction.setAgerating(resultSet.getString("ageRating"));
                    movieWithPrediction.setActors(resultSet.getString("actors"));
                    movieWithPrediction.setRuntime(resultSet.getInt("runtime"));
                    movieWithPrediction.setDirector(resultSet.getString("director"));
                    movieWithPrediction.setWriter(resultSet.getString("writer"));
                    movieWithPrediction.setMoviePosterURL(resultSet.getString("posterURL"));
                    movieWithPrediction.setPredictedRating(ARBITRARY_PREDICTION_RATING);
                    listOfMoviesWithPredictions.add(movieWithPrediction);
                }

                // Close connection and return
                conn.close();
                return listOfMoviesWithPredictions;

            } catch (SQLException e) {
                return null;
            }
        }


        private boolean isValidMovieWithPredictions(List<MovieWithPrediction> expectedMoviesWithPredictions, List<MovieWithPrediction> actualMoviesWithPredictions) {

            // Number of entries must match
            if (expectedMoviesWithPredictions.size() != actualMoviesWithPredictions.size()) {
                return false;
            }

            // Check all entries in the list
            for (int i = 0; i < expectedMoviesWithPredictions.size(); i++) {
                MovieWithPrediction expectedMovieWithPrediction = expectedMoviesWithPredictions.get(i);

                // It is OK for the list to be unsorted in this case, the PredictionEngineTest will need to check for sorting
                MovieWithPrediction actualMovieWithPrediction = null;
                for (MovieWithPrediction currentMovieWithPrediction : actualMoviesWithPredictions) {
                    if (currentMovieWithPrediction.getMovieID() == expectedMovieWithPrediction.getMovieID())
                        actualMovieWithPrediction = currentMovieWithPrediction;
                }

                // Check that all fields in Movie subclass are aligned
                if (!expectedMovieWithPrediction.equals(actualMovieWithPrediction)) {
                    return false;
                }
            }
            return true;
        }

    }

}