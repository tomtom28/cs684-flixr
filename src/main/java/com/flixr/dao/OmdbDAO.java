package com.flixr.dao;

import com.flixr.beans.Movie;
import com.flixr.beans.OmdbResponse;
import com.flixr.exceptions.OmdbException;
import org.springframework.web.client.RestTemplate;

import static com.flixr.configuration.ApplicationConstants.OMDB_API_KEY;


/**
 * @author Thomas Thompson
 *
 * Queries the IMDB API and then calls on the MovieDAO to add any new Movie records to the database
 */

public class OmdbDAO {

    public OmdbDAO() {}

    /**
     * @param imdbId    Movie Id as IMDB Id (ex. tt0114709)
     * @return Returns a Movie object, if the REST request was successful to the IMDB API
     * @throws OmdbException
     */
    public Movie getMovieFromOmdbAPI(String imdbId) throws OmdbException {

        // Query OMDB API
        String queryURL = "http://www.omdbapi.com/?i=" + imdbId + "&apikey=" + OMDB_API_KEY;
        RestTemplate restTemplate = new RestTemplate();
        OmdbResponse omdbResponse = restTemplate.getForObject(queryURL, OmdbResponse.class);

        // Query was unsuccessful
        if (omdbResponse.getImdbId() == null) {
            throw new OmdbException(new Exception("Invalid ImdbId! Unable to process request!"));
        }

        // Create Movie Object
        Movie movie = new Movie();
        movie.setMovieID(this.parseMovieIdFromImbdId(imdbId));
        movie.setMoviename(omdbResponse.getMovieTitle());
        movie.setReleasedate(omdbResponse.getReleaseDate());
        movie.setAgerating(omdbResponse.getAgeRating());
        movie.setActors(omdbResponse.getActors());
        movie.setRuntime(this.parseMovieRunTimeFromResponse(omdbResponse.getRunTime()));
        movie.setDirector(omdbResponse.getDirector());
        movie.setWriter(omdbResponse.getDirector());
        movie.setMoviePosterURL(omdbResponse.getMoviePosterURL());

        return movie;

    }

    /**
     * Alternate version of getting Movie from OMDB API
     * @param movieId   Movie Id (ex. 114709)
     * @return  Returns a Movie object, if the REST request was successful to the IMDB API
     * @throws OmdbException
     */
    public Movie getMovieFromOmdbAPI(int movieId) throws OmdbException {
        String imdbId = this.convertMovieIdToImdbId(movieId);
        return this.getMovieFromOmdbAPI(imdbId);
    }


    /**
     * Converts a MovieId to an ImdbId by prepending "0"s and "tt"
     * @param movieId   Movie Id from database (ex. 417)
     * @return  ImdbId conversion (ex. tt0000417)
     */
    protected String convertMovieIdToImdbId(int movieId) {

        String movieIdStr = Integer.toString(movieId);

        // Numerical Portion must be 7 digits long
        int movieIdDigitCount = movieIdStr.length();
        int numberOfExtraZerosNeededInPrefix = 7 - movieIdDigitCount;

        // Prepend # of Needed Zeros
        for (int i = 0; i < numberOfExtraZerosNeededInPrefix; i++) {
            movieIdStr = "0" + movieIdStr;
        }

        // Prepend "tt" to the movieId to create the ImdbId
        String imdbId = "tt" + movieIdStr;

        return imdbId;
    }

    /**
     * Convert and IMDB ID into a Movie ID
     * @param imdbId   IMDB ID (ex. tt0114709)
     * @return  Returns a Movie Id (ex. 114709)
     */
    private int parseMovieIdFromImbdId(String imdbId) throws OmdbException {
        // Remove the "tt" prefix and convert to integer MovieId
        try {
            String prefixRemoved = imdbId.substring(2);
            return Integer.parseInt(prefixRemoved);
        } catch (NumberFormatException e) {
            System.out.println("Unable to convert given ImdbId: " + imdbId + "into a movieId!");
            throw new OmdbException(e);
        }
    }

    /**
     * Convert and IMDB Movie Time into a Movie time integer
     * @param movieRunTime  MovieRunTime (ex. 180 min)
     * @return  Returns the integer value of the run time, assumed to be minutes (ex. 180)
     * @throws OmdbException
     */
    private int parseMovieRunTimeFromResponse(String movieRunTime) throws OmdbException {

        // Handle N/A Run Time
        if (movieRunTime.equals("N/A")) {
            return -1; // negative 1 will be N/A
        }

        try {
            int firstSpaceIndex = movieRunTime.indexOf(" ");
            String movieRunTimeValue = movieRunTime.substring(0, firstSpaceIndex);
            return Integer.parseInt(movieRunTimeValue);
        } catch (NumberFormatException e) {
            System.out.println("Unable to convert given RunTime: " + movieRunTime + "into an integer!");
            throw new OmdbException(e);
        }
    }

}
