package com.flixr.dao;

import com.flixr.beans.Movie;
import com.flixr.exceptions.DAOException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Author: Zion Whitehall
 */

public class MovieDAOTest
{/*
    @Test
    void testGetAllMovies()
    {
        try
        {
            MovieDAO movieDAO = new MovieDAO();
            List<Movie> AllMovies = movieDAO.generate(movie);
        }
        catch (Exception e)
        {
            fail("Unable to get all movies");
        }
    }


    /*@Test
    void testSaveMovies() throws DAOException {


        Movie movie = new Movie();
        movie.setMovieID(1);
        movie.setMoviename("Sonic");
        movie.setReleasedate("February 14 2020");
        movie.setAgerating("PG");
        movie.setActors("James Marsden " + "Ben Schwartz " + "Tika Sumpter " + "Jim Carrey ");
        movie.setRuntime(99);
        movie.setDirector("Jeff Fowler");
        movie.setWriter("Pat Casey" + "Josh Miller");
        movie.setMoviePosterURL("https://upload.wikimedia.org/wikipedia/en/c/c1/Sonic_the_Hedgehog_poster.jpg");

        movieDAO.saveMovie(movie);

        List<Movie> movies = movieDAO.getAllMovies();

        Assert.assertEquals(1, movie.getMovieID());
        Assert.assertEquals("Sonic", movie.getMoviename());
        Assert.assertEquals("February 14 2020", movie.getReleasedate());
        Assert.assertEquals("PG", movie.getAgerating());
        Assert.assertEquals("James Marsden \" + \"Ben Schwartz \" + \"Tika Sumpter \" + \"Jim Carrey ", movie.getActors());
        Assert.assertEquals(99, movie.getRuntime());
        Assert.assertEquals("Jeff Fowler", movie.getDirector());
        Assert.assertEquals("Pat Casey" + "Josh Miller", movie.getWriter());
        Assert.assertEquals("https://upload.wikimedia.org/wikipedia/en/c/c1/Sonic_the_Hedgehog_poster.jpg", movie.getposter_url());
    }*/
}
