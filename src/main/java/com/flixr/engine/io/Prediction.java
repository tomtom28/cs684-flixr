package com.flixr.engine.io;

/**
 * @author Thomas Thompson
 *
 * Prediction for a given user
 * Tuple of (MovieId, Predicted Rating) which can be sorted by the Predicted Rating
 */
public class Prediction implements Comparable {

    int movieId;
    double predictedRating;

    public Prediction(int movieId, double predictedRating) {
        this.movieId = movieId;
        this.predictedRating = predictedRating;
    }

    public int getMovieId() {
        return movieId;
    }

    public double getPredictedRating() {
        return predictedRating;
    }

    /**
     * Used to sort the Predictions based on Predicted Rating
     * @param   prediction    Prediction to be sorted against
     * @return  A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Object prediction) {
        // Cast to Prediction and compare based on Rating
        try {
            Prediction castedPrediction = (Prediction) prediction;
            if (this.getPredictedRating() > castedPrediction.getPredictedRating())
                return 1;
            else if (this.getPredictedRating() < castedPrediction.getPredictedRating())
                return -1;
        } catch (ClassCastException e) {
            System.out.println("Unable to compare Predictions!");
            e.printStackTrace();
        }
        return 0;
    }

}
