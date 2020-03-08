package com.flixr.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Thompson
 *
 * Used to store User Submissions, i.e. all movies that a given user has rated
 * The submissions is identifiec by the UserId and subsequent tuples for each (MovieId, Rating) they submitted
 * Passed as an input to the RecommendationEngine and PredictionEngine
 *
 */
public class UserSubmission implements Comparable {

    private int userId;

    // Two Lists are used to track user ratings, ratings and movies are at the same index
    // If: MoviesViewed = {10000, 100001, 100023} MovieRatings = {1.0, 4.0, 3.0}
    // Then: 100001 was rated as 4.0
    private List<Integer> moviesViewed;
    private List<Double> movieRatings;

    /**
     * Creates a User Submission
     * @param userId    User Id
     */
    public UserSubmission(int userId) {
        this.userId = userId;
        this.movieRatings = new ArrayList<>();
        this.moviesViewed = new ArrayList<>();
    }

    /**
     * Adds a (Movie, Rating) tuple to the user's submission
     * @param movieId   Movie Id
     * @param rating    Movie Rating
     */
    public void addMovieRating(int movieId, double rating) {

        moviesViewed.add(movieId);
        movieRatings.add(rating);

        // Must verify that indexes are aligned
        int lengthOfMovieList = moviesViewed.size();
        int lengthOfMovieRatings = movieRatings.size();
        assert lengthOfMovieList == lengthOfMovieRatings;
    }


    public int getUserId() {
        return userId;
    }

    public List<Integer> getMoviesViewed() {
        return moviesViewed;
    }

    public double getMovieRating(int movieId) {
        int movieIndex = moviesViewed.indexOf(movieId);
        return movieRatings.get(movieIndex);
    }


    /**
     * Used to sort the UserSubmissions based on UserId
     * @param userSubmission    UserSubmission
     * @return  A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Object userSubmission) {
        // Cast to UserSubmission and compare based on UserId
        try {
            UserSubmission castedUserSubmission = (UserSubmission) userSubmission;
            return this.getUserId() - castedUserSubmission.getUserId();
        } catch (ClassCastException e) {
            System.out.println("Unable to compare UserSubmissions!");
            e.printStackTrace();
        }
        return 0;
    }
}
