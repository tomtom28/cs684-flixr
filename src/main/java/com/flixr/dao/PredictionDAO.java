package com.flixr.dao;

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
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recenginemodel");
            ResultSet resultSet = stmt.executeQuery();
            System.out.println("Query Complete. Building Matrix... ");

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


}
