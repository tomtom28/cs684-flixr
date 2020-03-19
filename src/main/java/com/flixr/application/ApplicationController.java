/* Vraj Desai */
/*
package com.flixr.application;

import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.UserSubmission;
import com.flixr.dao.EngineDAO;
import com.flixr.dao.UserDAO;
import com.flixr.dao.MovieDAO;
import com.flixr.dao.RatingDAO;
import com.flixr.dao.PredictionDAO;
import com.flixr.engine.RecommendationEngine;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class ApplicationController {

    public ApplicationController(){

    }
    UserDAO userDAO = new UserDAO();
    UserSubmission userSubmission = engineDAO.getUserSubmission(userId);
    List<Users> = new userDAO.loginUser();
    List<Registered> new userDAO.signupUser();

    MovieDAO movieDAO = new MovieDAO();
    List<Movies> movies = Movie.getNextMovie(int userId);

    RatingDAO ratingDAO = new RatingDAO();
    List<StatsRating> = new ratingDAO.getMovieStatsByAverageRating();
    List<Title> = new ratingDAO.getMovieStatsByTitle();
    List<RatingCount> = new ratingDAO.getMovieStatsByRatingCount();

}

