package com.flixr.utils;

import com.flixr.beans.Movie;
import com.flixr.dao.OmdbDAO;
import com.flixr.exceptions.OmdbException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Thomas Thompson
 *
 * Runs a Utility to take all IMDB movieIds in a given list, query the OMDB for needed fields, and write the information to a CSV file.
 * The CSV is used to then populate our database Movies table
 */
public class OmdbDAOHarness {

    List<Integer> listOfMovieIds;

    private String fullInputFilePath;
    private String fullOutputFilePath;


    public OmdbDAOHarness(String fullInputFilePath, String fullOutputFilePath) {
        this.fullInputFilePath = fullInputFilePath;
        this.fullOutputFilePath = fullOutputFilePath;
    }


    // Gets a CSV friendly output for the Movie
    private String convertMovieBeanToCsvRow(Movie movie) {

        // FORMAT: MovieID,MovieName,ReleaseDate,AgeRating,Actors,Runtime,Director,Writer,PosterURL
        String movieData = new String("".getBytes(), UTF_8);
        movieData += "\"" + movie.getMovieID() + "\"" + ",";
        movieData += "\"" + movie.getMoviename() + "\"" + ",";
        movieData += "\"" + movie.getReleasedate() + "\"" + ",";
        movieData += "\"" + movie.getAgerating() + "\"" + ",";
        movieData += "\"" + movie.getActors() + "\"" + ",";
        movieData += "\"" + movie.getRuntime() + "\"" + ",";
        movieData += "\"" + movie.getDirector() + "\"" + ",";
        movieData += "\"" + movie.getWriter() + "\"" + ",";
        movieData += "\"" + movie.getposter_url() + "\"";

        return movieData;
    }

    // Sets of Movie IMDB ID's (taken from the input CSV file)
    private void setListOfMovieIdsFromCsv() {

        listOfMovieIds = new ArrayList<>();

        String line = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fullInputFilePath));
            bufferedReader.readLine(); // skips header row
            while ( (line = bufferedReader.readLine()) != null ) {

                // Assumes format: (UserId,MovieId,Rating)
                String movieIdStr = line;
                int movieId = Integer.parseInt(movieIdStr);

                // Add to list of MovieIds
                listOfMovieIds.add(movieId);
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Unable to read input file: \n" + fullInputFilePath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse the following line: \n" + line);
            e.printStackTrace();
        }
    }


    // Iterate over all MovieIds, Ping API, and Save Movie Data to CSV
    private void queryOmdbApiAndSaveToCSV() {

        // Initialize OMDB API DAO
        OmdbDAO omdbDAO = new OmdbDAO();

        // Initialize File Writer
        System.out.println("Saving API Data to CSV... ");
        PrintWriter writer = null;
        try {
            // Make Write & Print Header Row
            writer = new PrintWriter(fullOutputFilePath, "UTF-8");
            writer.println("MovieID,MovieName,ReleaseDate,AgeRating,Actors,Runtime,Director,Writer,PosterURL");

            // Iterate over all movieIds, hit API, and write results to CSV
            for (int movieId : listOfMovieIds) {
                try {
                    // Hit API to generate Movie bean
                    Movie movie = omdbDAO.getMovieFromOmdbAPI(movieId);

                    // Convert Movie bean fields to csv format
                    String movieDataRow = this.convertMovieBeanToCsvRow(movie);

                    // Write to CSV
                    writer.println(movieDataRow);

                    System.out.println("Completed Movie: " + (listOfMovieIds.indexOf( (Integer) movieId) + 1) + " of " + listOfMovieIds.size() + " MovieId = " + movieId);

                } catch (OmdbException e) {
                    System.out.println("Unable to fulfill OMDB API request!");
                    System.out.println("MovieID = " + movieId);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to save to CSV file!");
            e.printStackTrace();
        }
        finally {
            // Closes CSV output writer
            if (writer != null) writer.close();
        }

        System.out.println("API Data was saved to CSV.");

    }


    public static void main(String[] args) {

        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/test_data/omdb/movie-ids.csv";
        String outputFile = "/test_data/omdb/movie-outputs.csv";

        // Create Harness
        OmdbDAOHarness omdbDAOHarness = new OmdbDAOHarness(pathName + inputFile, pathName + outputFile);

        // Read Inputs
        omdbDAOHarness.setListOfMovieIdsFromCsv();

        // Query API and write to output CSV
        omdbDAOHarness.queryOmdbApiAndSaveToCSV();

    }

}
