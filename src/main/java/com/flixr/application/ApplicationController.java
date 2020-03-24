/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flixr.application;

/**
 * @author Thomas Thompson
 * API Connections to Front End
 */

import com.flixr.beans.*;
import com.flixr.dao.MovieDAO;
import com.flixr.dao.OmdbDAO;
import com.flixr.dao.RatingDAO;
import com.flixr.dao.UserDAO;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.exceptions.OmdbException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
public class ApplicationController {

	// Tracks user sessions
//	private static HashMap<String, UserSession> mapOfUserEmailToUserSession = new HashMap<>();

	// Renders the index.html page for ReactJS
	@GetMapping(value = "/")
	@ResponseBody
	public String index() {
		return "API is active.";
	}

	/**
	 * Sign Up the User
	 */
	@PostMapping("/signup")
	@ResponseBody
	public void signUpUser(	@RequestParam(name="email") String email,
						   	@RequestParam(name="password") String password,
							@RequestParam(name="fullname") String fullname,
							@RequestParam(name="age") int age,
						   	@RequestParam(name="country") String country) {
		// Insert User
		UserDAO userDAO = new UserDAO();
		userDAO.signUpUser(email, fullname, password, age, country);

		// Create User
//		User user = new User();
//		try {
//			UserDAO userDAO2 = new UserDAO();
//			user = userDAO2.signInUser(email, password);
//		} catch (DAOException e) {
//			System.out.println("Invalid User! Email and Password did not match!");
//		}
//		return user;

	}


	/**
	 * Sign in the User
	 * @return	User Bean
	 */
	@PostMapping("/signin")
	@ResponseBody
	public User signInUser(@RequestParam(name="email") String email,
						   @RequestParam(name="password") String password) {

		// Create User and User Session
		User user = new User();
		try {
			UserDAO userDAO = new UserDAO();
			user = userDAO.signInUser(email, password);
		} catch (DAOException e) {
			System.out.println("Invalid User! Email and Password did not match!");
		}
		return user;
	}


	/**
	 * Logout the User
	 * @param userEmail
	 * @return
	 */
	@GetMapping("/logout/{user_email}")
	@ResponseBody
	public HashMap<String, String> logOutUser(@PathVariable(value="user_email") String userEmail) {

		// Response
		HashMap<String, String> response = new HashMap<>();
		response.put("email", userEmail);
		response.put("status", "off");

		return response;
	}


	@GetMapping("/checkstatus/{user_email}")
	@ResponseBody
	public User checkUserStatus(@PathVariable(value="user_email") String email) {
		UserDAO userDAO = new UserDAO();
		User user = userDAO.checkUserStatus(email);
		return user;
	}


	@GetMapping("/rating/{user_email}/{index}")
	@ResponseBody
	public Movie getNextMovieToRate(@PathVariable(value="user_email") String userEmail,
									@PathVariable(value="index") int movieIndex) {

		try {
			// Select Movie by index
			MovieDAO movieDAO = new MovieDAO();
			List<Movie> allMovies = movieDAO.getAllMovies();
			Movie nextMovie = allMovies.get(movieIndex);
			return nextMovie;
		} catch (DAOException e) {
			System.out.println("Error! Cannot find any movies in database!");
			Movie errorMovie = new Movie();
			errorMovie.setMovieID(0);
			errorMovie.setMoviename("Database Error");
			errorMovie.setReleasedate("0000-00-00");
			errorMovie.setAgerating("N/A");
			return errorMovie;
		}
	}


	/**
	 * Submits a user's rating for a given movie
	 * @param userId
	 * @param movieId
	 * @param movieRating
	 */
	@PostMapping("/rating")
	@ResponseBody
	public void postMovieRating(@RequestParam(value="user_id") int userId,
								@RequestParam(name="movie_id") int movieId,
						   		@RequestParam(name="grade") double movieRating) {
		RatingDAO ratingDAO = new RatingDAO();
		ratingDAO.addMovieRating(userId, movieId, movieRating);
	}


	/**
	 * Gets the List of Top Rated Movies for a given User
	 * @param userId
	 * @return
	 */
	@GetMapping("/recommend/{user_id}/{sort_type}")
	@ResponseBody
	public List<MovieWithPrediction> getMovieRecommendations(@PathVariable(value="user_id") int userId,
															 @PathVariable(value="sort_type") String sortType) {

		// Parse Sort Types
		int numberOfMoviePredictions;
		boolean sortAlphabetic = false;
		if (sortType.equals("top100")) {
			numberOfMoviePredictions = 100;
		}
		else if (sortType.equals("top50")) {
			numberOfMoviePredictions = 50;
		}
		else if (sortType.equals("top25")) {
			numberOfMoviePredictions = 25;
		}
		else {
			numberOfMoviePredictions = 10;
		}

		// A to Z sort
		if (sortType.equals("a~z")) {
			numberOfMoviePredictions = 25;
			sortAlphabetic = true;
		}
		

		// Call on recommendation controller
		RecommendationController recommendationController = new RecommendationController();
		try {
			// Get Top N movies
			List<MovieWithPrediction> topMovieRecommendations = recommendationController.getTopMoviePredictions(userId, numberOfMoviePredictions);

			// Sort Movies Alphabetically if needed
			if(sortAlphabetic) {
				Collections.sort(topMovieRecommendations, new Comparator<MovieWithPrediction>() {
					@Override
					public int compare(MovieWithPrediction o1, MovieWithPrediction o2) {
						 return o1.getMoviename().compareToIgnoreCase(o2.getMoviename());
					}
				});
			}

			return topMovieRecommendations;
		} catch (EngineException | DAOException e) {
			System.out.println("Unable to send back a Movie Recommendation!");

			// Error, send back error movie
			List<MovieWithPrediction> errMovieList = new ArrayList<>();
			MovieWithPrediction errMoviePrediction = new MovieWithPrediction();
			errMoviePrediction.setMovieID(0);
			errMoviePrediction.setMoviename("Invalid Prediction");
			errMoviePrediction.setAgerating("N/A");
			errMoviePrediction.setPredictedRating(0.0);
			errMovieList.add(errMoviePrediction);
			return errMovieList;
		}

	}

	@GetMapping("/admin/analyze/{sort_type}")
	@ResponseBody
	public List<MovieStats> getAdminPage(@PathVariable(value="sort_type") String sortType) {
		RatingDAO ratingDAO = new RatingDAO();
		try {
			// Determine Results based on Sort Types
			if (sortType.equals("a~z")) {
				return ratingDAO.getMovieStatsByAtoZ();
			}
			else if (sortType.equals("z~a")) {
				return ratingDAO.getMovieStatsByZtoA();
			}
			else if (sortType.equals("count")) {
				return ratingDAO.getMovieStatsByTotalCount();
			}
			else { // default is rating
				return ratingDAO.getMovieStatsByAvgRating();
			}
		} catch(DAOException e) {
			System.out.println("Unable to generate MovieStats!");
			List<MovieStats> errMovieList = new ArrayList<>();
			MovieStats movieStats = new MovieStats(0,"Database Error",0,0);
			errMovieList.add(movieStats);
			return errMovieList;
		}

	}

	@GetMapping("/admin/re_train")
	@ResponseBody
	public String reTrainModel() {
		RecommendationController recommendationController = new RecommendationController();
		try {
			recommendationController.reTrainModel();
			return "Model is Now Training... Please Wait...";
		} catch (EngineException e) {
			return "Error in Training: " + e.getEngineMessage();
		}

	}


	@PostMapping("/admin/newmovie")
	@ResponseBody
	public String addNewMovie(@RequestParam(value="movie_id") String imdbId) {
		try {
			// Query OMDB API
			OmdbDAO omdbDAO = new OmdbDAO();
			Movie movie = omdbDAO.getMovieFromOmdbAPI(imdbId);

			// Save to Database
			MovieDAO movieDAO = new MovieDAO();
			movieDAO.saveMove(movie);

			System.out.println("MovieName: " + movie.getMoviename() + " added!");
			return "New Movie Added Successfully!";

		} catch (OmdbException e) {
			String msg = "Unable to Find Movie with IMdbId = " + imdbId;
			System.out.println(msg);
			return msg;
		} catch (DAOException e) {
			String msg = "Unable to Save Movie: " + e.getMessage();
			System.out.println(msg);
			return msg;
		}

	}

}