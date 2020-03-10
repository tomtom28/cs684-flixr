package com.flixr.beans;

/**
 * @author Thomas Thompson
 *
 * Adds a Predicted Rating variable to the Movie bean
 *
 */
public class MovieWithPrediction extends Movie {

    private double predictedRating;

    public double getPredictedRating() {
        return predictedRating;
    }

    public void setPredictedRating(double predictedRating) {
        this.predictedRating = predictedRating;
    }
}
