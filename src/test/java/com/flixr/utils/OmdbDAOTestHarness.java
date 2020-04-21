package com.flixr.utils;

import com.flixr.beans.Movie;
import com.flixr.dao.OmdbDAO;
import com.flixr.exceptions.OmdbException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Test Harness to run OmdbDAO repeatedly to collect Movie data from OMDB API
 *
 * Runs a Utility to take all IMDB movieIds in a given list, query the OMDB for needed fields, and write the information to a CSV file.
 * The output data from this test (a CSV file) was used to seed our database
 */
class OmdbDAOTestHarness {

    private OmdbDAOHarnessTestDriver omdbDAOHarnessTestDriver;
    private OmdbDAOHarnessTestOracle omdbDAOHarnessTestOracle;

    @BeforeEach
    void init() {
        omdbDAOHarnessTestDriver = new OmdbDAOHarnessTestDriver();
        omdbDAOHarnessTestOracle = new OmdbDAOHarnessTestOracle();
    }


    /**
     * @author Thomas Thompson
     * Test ID: 5
     * Test Type: Integration
     * Test Name: OmdbDAOTestHarness-1
     *
     * Collect a list of MovieIds (from CSV file) and query the 3rd party OMDB API to get all movie data
     * All outputs are collected into a CSV output file
     */
    @Test
    public void testGenerateMoviesCsvFile() {

        try {
            // Read in movieIds from Test Driver & get output path
            omdbDAOHarnessTestDriver.setListOfMovieIdsFromCsv();
            List<Integer> listOfMovieIds = omdbDAOHarnessTestDriver.getListOfMovieIds();
            String outputPath = omdbDAOHarnessTestDriver.getFullOutputFilePath();

            // Run all movies through OmdbDAO
            omdbDAOHarnessTestOracle.initialize(listOfMovieIds, outputPath);
            omdbDAOHarnessTestOracle.queryOmdbApiAndSaveToCSV();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error: "+ e.getMessage());
        }

    }

    /**
     * @author Thomas Thompson
     *
     * Test Driver to read in CSV file of IMDB IDs
     */
    private class OmdbDAOHarnessTestDriver {

        // Instance Variables
        List<Integer> listOfMovieIds;
        private String fullInputFilePath;
        private String fullOutputFilePath;


        // Set all Test Driver Variables here:
        // .........................................................................................................
        // Selects a Input/Output CSV Files
        String pathName = System.getProperty("user.dir");
        String inputFile = "/src/test/resources/omdb/inputs/movie-ids.csv";
        String outputFile = "/src/test/resources/omdb/outputs/movie-outputs.csv";
        // .........................................................................................................


        public OmdbDAOHarnessTestDriver() {
            this.fullInputFilePath = pathName + inputFile;
            this.fullOutputFilePath = pathName + outputFile;
        }


        // Sets of Movie IMDB ID's (taken from the input CSV file)
        private void setListOfMovieIdsFromCsv() throws Exception {

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
                throw e;
            } catch (NumberFormatException e) {
                System.out.println("Unable to parse the following line: \n" + line);
                throw e;
            }
        }


        public List<Integer> getListOfMovieIds() {
            return listOfMovieIds;
        }

        public String getFullOutputFilePath() {
            return fullOutputFilePath;
        }

    }


    /**
     * @author Thomas Thompson
     *
     * Test Oracle for OmdbDAOHarness
     */
    private class OmdbDAOHarnessTestOracle {

        // Instance Variables
        List<Integer> listOfMovieIds;
        private String fullOutputFilePath;


        public void initialize(List<Integer> listOfMovieIds,  String fullOutputFilePath) {
            this.listOfMovieIds = listOfMovieIds;
            this.fullOutputFilePath = fullOutputFilePath;
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
                        String message = "Unable to fulfill OMDB API request for MovieID = " + movieId;
                        System.out.println(message);
                        e.printStackTrace();
                        fail(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                fail("Unable to save movie information to CSV file!");
            }
            finally {
                // Closes CSV output writer
                if (writer != null) writer.close();
            }

            System.out.println("API Data was saved to CSV.");

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

    }

}