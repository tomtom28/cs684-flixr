package com.flixr.engine.utils;

/**
 * @author Thomas Thompson
 *
 * Used to store UserId, MovieId, and Ratings
 * Passed as an input to the RecommendationEngine
 *
 */
public class EngineInput {

    private int userId;
    private int movieId;
    private double rating;

    public EngineInput(int userId, int movieId, double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public double getRating() {
        return rating;
    }
}
