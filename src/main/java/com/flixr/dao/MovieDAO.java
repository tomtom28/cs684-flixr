package com.flixr.dao;
/**
 * Author: Zion Whitehall
 * DAO to query the database and return all movies
 */

import com.flixr.beans.Movie;
import com.flixr.exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;

import static com.flixr.configuration.ApplicationConstants.*;

public class MovieDAO {

    public ArrayList <Movie> getAllMovies() throws DAOException //return all movies
    {
        //query database and get all movies
        //iterate over results set

        try {
            String query = "SELECT * FROM movies ORDER BY MovieId DESC"; //takes everything from movies table

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            // Iterate over ResultSet to create list of MovieIds
            ArrayList<Movie> allMovies = new ArrayList<Movie>(); //create new Arraylist to send a list of all movie object

            //do git pull later for movie stuff in beans
            //look at what's in the movie object and pull whatever it is asking for

            //making my own while loop to get the stuff from the DB
            while (resultSet.next())
            {
                // Make the Movie Bean
                Movie movie = new Movie();
                movie.setMovieID(resultSet.getInt("movieID"));
                movie.setMoviename(resultSet.getString("movieName"));
                movie.setReleasedate(resultSet.getString("releaseDate"));
                movie.setAgerating(resultSet.getString("ageRating"));
                movie.setActors(resultSet.getString("actors"));
                movie.setRuntime(resultSet.getInt("runtime"));
                movie.setDirector(resultSet.getString("director"));
                movie.setWriter(resultSet.getString("writer"));
                movie.setMoviePosterURL(resultSet.getString("posterURL"));

                // Add the Movie Bean to the List
                allMovies.add(movie);
            }


            // Close connection and return
            conn.close();
            return allMovies;

        }

        catch (SQLException e)
        {
            throw new DAOException(e);
        }
    }

    public ArrayList <Movie> getUnderageMoviesOnly() throws DAOException //return all movies
    {
        //query database and get all movies
        //iterate over results set

        try {
            String query = "SELECT * FROM movies WHERE AgeRating NOT IN" +
                    "(UNRATED, Not Rated, R, M, M/PG, X, TV-MA) ORDER BY MovieId DESC";

            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet resultSet = stmt.executeQuery();

            // Iterate over ResultSet to create list of MovieIds
            ArrayList<Movie> allMovies = new ArrayList<Movie>(); //create new Arraylist to send a list of all movie object

            //do git pull later for movie stuff in beans
            //look at what's in the movie object and pull whatever it is asking for

            //making my own while loop to get the stuff from the DB
            while (resultSet.next())
            {
                // Make the Movie Bean
                Movie movie = new Movie();
                movie.setMovieID(resultSet.getInt("movieID"));
                movie.setMoviename(resultSet.getString("movieName"));
                movie.setReleasedate(resultSet.getString("releaseDate"));
                movie.setAgerating(resultSet.getString("ageRating"));
                movie.setActors(resultSet.getString("actors"));
                movie.setRuntime(resultSet.getInt("runtime"));
                movie.setDirector(resultSet.getString("director"));
                movie.setWriter(resultSet.getString("writer"));
                movie.setMoviePosterURL(resultSet.getString("posterURL"));

                // Add the Movie Bean to the List
                allMovies.add(movie);
            }


            // Close connection and return
            conn.close();
            return allMovies;

        }

        catch (SQLException e)
        {
            throw new DAOException(e);
        }
    }

    public void saveMovie(Movie movie) throws DAOException //method puts all movies into getters
    {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            String query = "INSERT INTO movies(MovieId, MovieName, ReleaseDate, AgeRating, Actors, RunTime, Director, Writer, PosterURL) VALUES (?,?,?,?,?,?,?,?,?)"; //takes everything from movies table
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, movie.getMovieID());
            stmt.setString(2, movie.getMoviename());
            stmt.setString(3, movie.getReleasedate());
            stmt.setString(4, movie.getAgerating());
            stmt.setString(5, movie.getActors());
            stmt.setInt(6, movie.getRuntime());
            stmt.setString(7, movie.getDirector());
            stmt.setString(8, movie.getWriter());
            stmt.setString(9, movie.getposter_url());
            stmt.executeUpdate();
        }

        catch (SQLException e)
        {
            throw new DAOException(e);
        }
    }

}

//do the same thing with UserDAO
//look at application controller and figure out what is needed
//Take in movieId, query api, get all fields I need, then add them