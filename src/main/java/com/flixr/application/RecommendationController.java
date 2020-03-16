package com.flixr.application;


import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.UserSubmission;
import com.flixr.dao.EngineDAO;
import com.flixr.dao.PredictionDAO;
import com.flixr.engine.PredictionEngine;
import com.flixr.beans.Prediction;
import com.flixr.engine.RecommendationEngine;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * Calls on Recommendation Engine for training new models
 * Calls on Prediction Engine for getting movie recommendations
 * Calls on EngineDAO for getting results based on an existing model
 */
public class RecommendationController {

    public RecommendationController() {
    }

    /**
     * Returns a list of top "X" movie predictions, sorted by highest to lowest predicted rating
     * @param userId                    UserId of user
     * @param numberOfMoviePredictions  "X" Number of predictions (ex. top "10")
     * @return Returns the top "X" movie predictions
     * @throws SQLException                 Thrown if DB Connection issue
     * @throws IndexOutOfBoundsException    Thrown if top "X" is greater than the number of predicted movies available
     */
    public List<MovieWithPrediction> getTopMoviePredictions(int userId, int numberOfMoviePredictions) throws EngineException, DAOException, IndexOutOfBoundsException {

        // Get UserSubmission
        EngineDAO engineDAO = new EngineDAO();
        UserSubmission userSubmission = engineDAO.getUserSubmission(userId);

        // Get MovieIds not rated by user
        Collection<Integer> movieIdsNotRatedByUser = engineDAO.getMovieIdsNotRatedByUserId(userId);

        // Generate Prediction from Engine
        PredictionDAO predictionDAO = new PredictionDAO();
        PredictionEngine predictionEngine = new PredictionEngine(userSubmission, movieIdsNotRatedByUser, predictionDAO);
        predictionEngine.generatePredictions();

        // Get Top "X" movie predictions
        List<Prediction> predictions = predictionEngine.getTopXMoviePredictions(numberOfMoviePredictions);

        // Iterate over predictions to create MoviePredictions
        List<MovieWithPrediction> predictedMovies = predictionDAO.getPredictedMovies(predictions);

        return predictedMovies;
    }


    /**
     * Launches the Recommendation Engine to begin a new training session
     * This is a very time & CPU intensive process. Use with caution.
     */
    public void reTrainModel() throws EngineException{

        try {
            // Get a list of all Rated Movies
            EngineDAO engineDAO = new EngineDAO();
            TreeSet<Integer> sortedListOfMovieIds = engineDAO.getDistinctMovieIds();

            // Generate all UserSubmissions
            TreeMap<Integer, UserSubmission> sortedListOfUserSubmissions = engineDAO.getAllUserSubmissions();

            // Launch a new instance of the Recommendation Engine
            RecommendationEngine recommendationEngine = new RecommendationEngine(sortedListOfMovieIds);

            // Train Model
            recommendationEngine.trainModel(sortedListOfUserSubmissions);

        } catch (DAOException e) {
            System.out.println("Unable to get Distinct Movie Ids!");
            e.printStackTrace();
            throw new EngineException(e);
        }
    }

}
