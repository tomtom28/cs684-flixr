package com.flixr.threads;

import com.flixr.beans.UserSubmission;
import com.flixr.engine.RecommendationEngine;
import com.flixr.exceptions.EngineException;

import java.util.Map;
import java.util.TreeSet;

/**
 * @author Thomas Thompson
 *
 * This thread is used by the Recommendation Controller to run multiple instances of the movie Correlation Matrix
 * Each Engine instance runs in parallel, using a subset of the movie Ids and all user submissions
 */
public class RecEngineThread extends Thread {

    private RecommendationEngine recommendationEngine;

    // Toggle between saving to CSV vs. MySQL (default)
    private boolean saveToCSV;
    private String fullOutputPath;

    /**
     * Runs a Parallel Engine Instance for Multi-Threaded Support (with DB model saving)
     * @param engineNumber  Thread Number (ex. 1)
     * @param listOfDistinctMovieIds    List of MovieId subset
     * @param userSubmissions   List of all UserSubmissions (ie all movies rated by users)
     */
    public RecEngineThread(int engineNumber, TreeSet<Integer> listOfDistinctMovieIds, Map<Integer, UserSubmission> userSubmissions) {
        recommendationEngine = new RecommendationEngine(listOfDistinctMovieIds);
        recommendationEngine.setUserSubmissions(userSubmissions);
        recommendationEngine.setEngineNumber(engineNumber);
    }

    /**
     * Runs a Parallel Engine Instance for Multi-Threaded Support (with CSV model saving)
     * @param engineNumber  Thread Number (ex. 1)
     * @param listOfDistinctMovieIds    List of MovieId subset
     * @param userSubmissions   List of all UserSubmissions (ie all movies rated by users)
     * @param fullOutputPath    CSV File Output Path
     */
    public RecEngineThread(int engineNumber, TreeSet<Integer> listOfDistinctMovieIds, Map<Integer, UserSubmission> userSubmissions, String fullOutputPath) {
        this(engineNumber, listOfDistinctMovieIds, userSubmissions);
        this.saveToCSV = true;
        this.fullOutputPath = fullOutputPath;
    }

    @Override
    public void run() throws RuntimeException {
        try {
            // Compute the Correlation Matrix
            recommendationEngine.generateCorrelationMatrix();

            // Either Save to CSV or Database
            if (saveToCSV) {
                recommendationEngine.saveModelToCSV(fullOutputPath);
            }
            else {
                recommendationEngine.saveModelToDB();
            }
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

}
