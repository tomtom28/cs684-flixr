package com.flixr.application;


import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.UserSubmission;
import com.flixr.dao.EngineDAO;
import com.flixr.dao.PredictionDAO;
import com.flixr.engine.PredictionEngine;
import com.flixr.beans.Prediction;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.threads.RecEngineThread;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flixr.configuration.ApplicationConstants.*;

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

            // Support Multi Threading by splitting up rated movies
            int[] splitIndxs = this.getMatrixSplitPoints(sortedListOfMovieIds);

            // Convert TreeSet to ArrayList (to facilitate splitting by index)
            List<Integer> listOfDistinctMovieIds = new ArrayList<>(sortedListOfMovieIds);

            // Spawn Threads for faster run time
            ExecutorService executor = Executors.newFixedThreadPool(REC_ENGINE_THREADS);
            try {
                for (int i = 1; i <= REC_ENGINE_THREADS; i++) {
                    // Create a subset of the movies for Rec Engine Thread
                    TreeSet<Integer> subsetOfDistinctMovieIds = new TreeSet<>( listOfDistinctMovieIds.subList(splitIndxs[i-1], splitIndxs[i]) ) ;

                    // Instantiate the given Engine Thread (select between DB or CSV mode)
                    RecEngineThread recEngineThread;
                    if (USE_CSV_MATRIX) {
                        String matrixFilePrefix = CSV_MATRIX_FILE_PATH + CSV_MATRIX_FILE_PREFIX + "-" + i + "-of-" + REC_ENGINE_THREADS;
                        recEngineThread = new RecEngineThread(i, subsetOfDistinctMovieIds, sortedListOfMovieIds, sortedListOfUserSubmissions, matrixFilePrefix);
                    }
                    else {
                        recEngineThread = new RecEngineThread(i, subsetOfDistinctMovieIds, sortedListOfMovieIds, sortedListOfUserSubmissions);
                    }

                    // Run a service to compute a subset of the matrix, using the given Engine Thread
                    executor.execute(recEngineThread);
                }

                // Wait for threads to complete
                executor.shutdown();
                while (!executor.isTerminated()) {}

            } catch (RuntimeException e) {
                System.out.println("Unable to compute Correlation Matrix! Problem encountered within Threads!");
                e.printStackTrace();
                throw new EngineException(e);
            }

        } catch (DAOException e) {
            System.out.println("Unable to get Distinct Movie Ids!");
            e.printStackTrace();
            throw new EngineException(e);
        }
    }


    /**
     * Determines the Start / End indices that evenly divide the full list of MovieIds
     * The list will be divided by # of REC_ENGINE_THREADS
     *
     * @param sortedListOfMovieIds  List of all Movie Ids
     * @return  Indexes of the Movie Ids List that dictate start/end points of the subset lists
     */
    private int[] getMatrixSplitPoints(TreeSet<Integer> sortedListOfMovieIds) {
        // Determine # of entries per subset of movie list (partitioned by thread count)
        int dividedCount = 1;
        try {
            dividedCount = (int) Math.floor( (double) sortedListOfMovieIds.size() / REC_ENGINE_THREADS ) ;
        } catch (ArithmeticException e) {
            System.out.println("Warning: Number of Recommendation Engine Threads must be at least 1!");
            System.out.println("Proceeding with a single-threaded configuration: REC_ENGINE_THREADS = 1");
        }

        // Determine split indices for new movie list sublists
        int currentIndx = 0;
        int[] splitIndxs = new int[REC_ENGINE_THREADS + 1];
        for (int i = 0; i < splitIndxs.length - 1; i++) {
            splitIndxs[i] = currentIndx;
            currentIndx += dividedCount;
        }
        splitIndxs[splitIndxs.length - 1] = sortedListOfMovieIds.size(); // any remainders will just get tacked on to the last thread

        return splitIndxs;
    }

}
