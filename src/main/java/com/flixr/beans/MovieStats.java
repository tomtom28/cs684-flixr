package com.flixr.beans;

/**
 * @author Thomas Thompson
 *
 * Creates a Usage Statistics object for a given Movie
 *
 */
public class MovieStats {

    private int movieId;
    private String movieTitle;
    private int totalRatingCount;
    private double averageRating;

    public MovieStats() {}

    public MovieStats(int imdbId, String movieTitle, int totalRatingCount, double averageRating) {
        this.movieId = imdbId;
        this.movieTitle = movieTitle;
        this.averageRating = averageRating;
        this.totalRatingCount = totalRatingCount;
    }

    public int getMovie_id() {
        return movieId;
    }

    public String getTitle() {
        return movieTitle;
    }

    public int getCount() {
        return totalRatingCount;
    }

    public double getRating() {
        return averageRating;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public void setTotalRatingCount(int totalRatingCount) {
        this.totalRatingCount = totalRatingCount;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }



}
