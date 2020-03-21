package com.flixr.dao;

import com.flixr.beans.MovieStats;

public class RatingDAO
{
    public void addMovieRating(int userID, int imdbID, double rating)
    {
        // insert query to database rating table
        String query = "SELECT * " +
                "FROM ratings "; //takes everything from movies table
    }

    public ArrayList<MovieStats> getMovieStatsByAvgRating()
    {
        // SELECT QUERY to get all movies (like in MovieDAO) but then also added DB stats
        // Might neeed a SQL GROUP BY imdbId query
        // use AVG(rating) and Count(Rating)
        // ORDER BY MovieTitle (or however its called in the DB)
    }
}
