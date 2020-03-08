package com.flixr.engine;

import com.flixr.interfaces.IPredictionEngineDAO;
import com.flixr.beans.Prediction;
import com.flixr.beans.UserSubmission;


import java.util.*;

/**
 * @author Thomas Thompson
 *
 * PredictionEngineInput Engine:
 * Used to generate movie predictions for a given UserId
 * Requires a trained model (i.e. correlation matrix) from the Recommendation Engine
 * Uses the Slope One algorithm
 *
 * All code is original, however the following source was referenced for Pseudo-Code / Design Decisions:
 * http://girlincomputerscience.blogspot.com/search/label/Recommender%20Systems
 *
 * The output of this model gives a sorted list of Predictions
 */

public class PredictionEngine {

    private IPredictionEngineDAO predictionEngineDAO;

    private UserSubmission userSubmission;
    private Collection<Integer> movieIdsRatedByUser;
    private Collection<Integer> movieIdsNotRatedByUser;
    private int numberOfMoviesRatedByUser;

    private List<Prediction> totalPredictions;

    public PredictionEngine(UserSubmission userSubmission, Collection<Integer> movieIdsNotRatedByUser, IPredictionEngineDAO predictionEngineDAO) {
        this.userSubmission = userSubmission;
        this.movieIdsNotRatedByUser = movieIdsNotRatedByUser;
        this.movieIdsRatedByUser = userSubmission.getMoviesViewed();
        this.numberOfMoviesRatedByUser = movieIdsRatedByUser.size();
        this.predictionEngineDAO = predictionEngineDAO;
        this.totalPredictions = new ArrayList<>();
    }


    public void generatePredictions() {

        // For every item i the user u expresses no preference for:
        for (int movieIdNotRated : movieIdsNotRatedByUser) {

            // For every item j that user u expresses a preference for:
            double sumOfPredictedRatingsForSelectedMovie = 0.0;
            for (int movieIdRated : movieIdsRatedByUser) {

                // Find the average preference difference between j and i
                double avgPreferenceDifference = predictionEngineDAO.getAveragePreferenceDifference(movieIdRated, movieIdNotRated);

                // Add this diff to u’s preference value for j
                double userPreference = userSubmission.getMovieRating(movieIdRated) + avgPreferenceDifference;

                // Add to a running total
                sumOfPredictedRatingsForSelectedMovie += userPreference;

            }

            // Add this to a running average
            double predictedRating = sumOfPredictedRatingsForSelectedMovie / numberOfMoviesRatedByUser;

            // Create a new Prediction
            Prediction prediction = new Prediction(movieIdNotRated, predictedRating);
            totalPredictions.add(prediction);
        }

        // Sort Predictions Highest to Lowest
        Collections.sort(totalPredictions, Collections.reverseOrder());

    }


    public List<Prediction> getTopXMoviePredictions(int numberOfMovies) {
        List<Prediction> predictions = new ArrayList<>();
        for (int i = 0; i < numberOfMovies; i++) {
            predictions.add(totalPredictions.get(i));
        }
        return predictions;
    }


    public List<Prediction> getAllMoviePredictions() {
        return totalPredictions;
    }

}
