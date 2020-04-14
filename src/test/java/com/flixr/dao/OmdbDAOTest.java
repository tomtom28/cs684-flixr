package com.flixr.dao;

import com.flixr.beans.Movie;
import com.flixr.exceptions.OmdbException;
import org.junit.Ignore;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 * Unit Tests related to the OMDB API; i.e. retreiving data from a third party API
 */
class OmdbDAOTest {

    @Nested
    class TestOmdbApiQueries {

        /**
         * Thomas Thompson, Test # 7
         * Test Name: Unit-7
         *
         * Ensure that OMDB API is successfully queried & that correct attributes are returned for the selected ImdbId
         */
        @Test
        void getMovieFromOmdbAPI() {
            // Query OMDB API for new Movie
            String imdbId = "tt0114709";
            try {
                OmdbDAO omdbDAO = new OmdbDAO();
                Movie movie = omdbDAO.getMovieFromOmdbAPI(imdbId);
                // Check that key movie attributes match:
                String expectedMovieName = "Toy Story";
                String expectedReleaseDate = "22 Nov 1995";
                String expectedAgeRating = "G";
                assertEquals(expectedMovieName, movie.getMoviename());
                assertEquals(expectedAgeRating, movie.getAgerating());
                assertEquals(expectedReleaseDate, movie.getReleasedate());
            } catch (OmdbException e) {
                fail("Unable to process API request: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Thomas Thompson, Test # 8
         * Test Name: Unit-8
         *
         * Ensure that an exception is thrown if an invalid ImdbId is sent to the OmbdDAO
         */
        @Test
        void getInvalidMovieFromOmdbAPI() {
            String invalidId = "tt404404";
            // Query OMDB API for new Movie
            OmdbDAO omdbDAO = new OmdbDAO();
            assertThrows(OmdbException.class, () -> omdbDAO.getMovieFromOmdbAPI(invalidId));
        }
    }

    /**
     * Thomas Thompson, Test # 9
     * Test Name: Unit-9
     *
     * Ensure that the OmdbDAO supports conversions between movieId (i.e. integer format) and ImdbId (i.e. string format)
     */
    @Nested
    class TestOmdbApiMovieIdConversions {

        @Test
        void testMovieIdToImdbId3Digits() {
            int movieId = 417;
            String expectedImdbId = "tt0000417";
            OmdbDAO omdbDAO = new OmdbDAO();
            assertEquals(expectedImdbId, omdbDAO.convertMovieIdToImdbId(movieId));
        }

        @Test
        void testMovieIdToImdbId8Digits() {
            int movieId = 114709;
            String expectedImdbId = "tt0114709";
            OmdbDAO omdbDAO = new OmdbDAO();
            assertEquals(expectedImdbId, omdbDAO.convertMovieIdToImdbId(movieId));
        }

        @Test
        void testMovieIdToImdbId9Digits() {
            int movieId = 1133093;
            String expectedImdbId = "tt1133093";
            OmdbDAO omdbDAO = new OmdbDAO();
            assertEquals(expectedImdbId, omdbDAO.convertMovieIdToImdbId(movieId));
        }

    }

}