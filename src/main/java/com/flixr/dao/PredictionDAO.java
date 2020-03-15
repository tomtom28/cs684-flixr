package com.flixr.dao;

import com.flixr.beans.Movie;
import com.flixr.beans.MovieWithPrediction;
import com.flixr.beans.Prediction;
import com.flixr.exceptions.DAOException;
import com.flixr.exceptions.EngineException;
import com.flixr.interfaces.IPredictionEngineDAO;

import java.sql.*;
import java.util.*;

import static com.flixr.configuration.ApplicationConstants.*;

public class PredictionDAO implements IPredictionEngineDAO {

    private int totalCountOfMoviesInMatrix;
    private Set<Integer> distinctMovieIds;
    private double[][] correlationMatrix;
    private HashMap<Integer, Integer> movieIdToMatrixIndex; // MovieId -> Index

    /**
     * Constructor without in memory storage
     * Used for much faster querying / prediction generation
     */
    public PredictionDAO() throws DAOException {
        setTotalCountOfMoviesInMatrix();
        setDistinctMovieIds();
        generateMatrixModel();
    }

    /**
     * Gets the Correlation between Movies in the trained Recommendation Model
     * @param movieId_i     MovieId in Matrix position i
     * @param movieId_j     MovieId in Matrix position j
     * @return  Correlation (i.e. average preference difference between Movie i and Movie j)
     */
    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) throws EngineException {
        try {
            // Convert MovieId to MatrixIndex
            int i = movieIdToMatrixIndex.get(movieId_i);
            int j = movieIdToMatrixIndex.get(movieId_j);

            // Return Average Difference
            return correlationMatrix[i][j];

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Matrix Index was Invalid!");
            e.printStackTrace();
            throw new EngineException(e);
        }
    }


    private void generateMatrixModel() throws DAOException {

        // Track Progress
        System.out.println("Loading Correlation Matrix... ");

        // Initialize Matrix & Index Map
        correlationMatrix = new double[totalCountOfMoviesInMatrix][totalCountOfMoviesInMatrix];
        movieIdToMatrixIndex = new HashMap<>();

        // Map MovieId to Matrix Index
        int matrixIndx = 0;
        for (int movieId: distinctMovieIds) {
            movieIdToMatrixIndex.put(movieId, matrixIndx);
            matrixIndx++;
        }

        // Query Database to Generate Correlation Matrix
        try {
            System.out.println("Querying Database... ");
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
//            Statement stmt = conn.createStatement();

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recenginemodel WHERE movieIdi = ?");

            // TODO : Try to improve performance by using threads

            // Counter for logging
            int count = 0;

            // Iterate over all stored Rows in Matrix
            for (int movieIdi : distinctMovieIds) {

                stmt.setInt(1, movieIdi);
                ResultSet resultSet = stmt.executeQuery();

                // Iterate Over all DB Entries
                while (resultSet.next()) {

                    // Assumes format: (MovieId_i,MovieId_j,Rating)
                    int movieIdMatrix_i = resultSet.getInt("MovieIdi");
                    int movieIdMatrix_j = resultSet.getInt("MovieIdj");
                    double avgRatingDifference = resultSet.getDouble("AvgDifference");;

                    // Convert MovieId to Matrix Index
                    int i = movieIdToMatrixIndex.get(movieIdMatrix_i);
                    int j = movieIdToMatrixIndex.get(movieIdMatrix_j);

                    // Add to internal matrix
                    correlationMatrix[i][j] = avgRatingDifference;
                }
                count++;
                System.out.println("Completed Matrix Row: " + count + " of " + distinctMovieIds.size());
            }

            // Completed Loading Matrix, close connection
            System.out.println("Query Complete. Building Matrix... ");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException(e);
        }

        System.out.println("Correlation Matrix Loaded.");
    }


    private void setDistinctMovieIds() throws DAOException {
        distinctMovieIds = new TreeSet<>();
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT MovieIDi FROM recenginemodel ORDER BY MovieIdi");
            ResultSet resultSet = stmt.executeQuery();

            // Iterate Over MovieIds
            while (resultSet.next()) {
                int movieId = resultSet.getInt("MovieIDi");
                distinctMovieIds.add(movieId);
            }

            conn.close();

        } catch (SQLException e) {
            throw new DAOException(e);
        }

    }

    /**
     * Determines the total # of Movies in the Matrix
     * @throws DAOException
     */
    private void setTotalCountOfMoviesInMatrix() throws DAOException {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS MovieCount FROM (SELECT DISTINCT MovieIDi FROM recenginemodel) Q1");
            ResultSet resultSet = stmt.executeQuery();

            resultSet.next();
            totalCountOfMoviesInMatrix = resultSet.getInt("MovieCount");

            conn.close();
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Create Movies with Predicted Ratings
     * @param predictions   List of Predictions from the Prediction Engine
     * @return  List of MoviesWithPredictions, aggregates Movie information with the Predicted Ratings
     * @throws DAOException
     */
    public List<MovieWithPrediction> getPredictedMovies(List<Prediction> predictions) throws DAOException {
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM movies WHERE movieId = ?");

            // Iterate Over Predictions & Use MovieID to query for Movie data
            List<MovieWithPrediction> moviesWithPredictions = new ArrayList<>();
            for (Prediction prediction : predictions) {
                // Get Movie Data
                stmt.setInt(1, prediction.getMovieId());
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();

                // Create new Movie with Prediction
                MovieWithPrediction movieWithPrediction = new MovieWithPrediction();
                movieWithPrediction.setMovieID(resultSet.getInt("movieID"));
                movieWithPrediction.setMoviename(resultSet.getString("movieName"));
                movieWithPrediction.setReleasedate(resultSet.getString("releaseDate"));
                movieWithPrediction.setAgerating(resultSet.getString("ageRating"));
                movieWithPrediction.setActors(resultSet.getString("actors"));
                movieWithPrediction.setRuntime(resultSet.getInt("runtime"));
                movieWithPrediction.setDirector(resultSet.getString("director"));
                movieWithPrediction.setWriter(resultSet.getString("writer"));
                movieWithPrediction.setMoviePosterURL(resultSet.getString("moviePosterURL"));

                // Set the Predicted Rating
                movieWithPrediction.setPredictedRating(prediction.getPredictedRating());

                // Append to List
                moviesWithPredictions.add(movieWithPrediction);
            }

            // Close and return
            conn.close();
            return moviesWithPredictions;

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }




}
