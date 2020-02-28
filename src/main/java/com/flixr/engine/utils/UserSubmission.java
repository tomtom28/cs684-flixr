package com.flixr.engine.utils;

import java.util.HashMap;

/**
 * @author Thomas Thompson
 *
 * Used to store UserId
 * Maps the UserId to all Movies they Rated
 *
 */
public class UserSubmission {

    private int userId;
    private HashMap<Integer, Double> mapOfMovieIdToRating;

    public UserSubmission(int userId, int movieId, double rating) {
        this.userId = userId;

    }

}
