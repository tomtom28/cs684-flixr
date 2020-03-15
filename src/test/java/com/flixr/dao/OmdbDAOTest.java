package com.flixr.dao;

import com.flixr.beans.Movie;
import com.flixr.exceptions.OmdbException;
import org.junit.Ignore;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 */
class OmdbDAOTest {

    @Nested
    class TestOmdbApiQueries {

        @Test
        void getMovieFromOmdbAPI() {

            String imdbId = "tt0114709";

            // Query OMDB API for new Movie
            try {
                OmdbDAO omdbDAO = new OmdbDAO();
                Movie movie = omdbDAO.getMovieFromOmdbAPI(imdbId);
                // TODO might need deeper testing check
            } catch (OmdbException e) {
                fail("Unable to process API request: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Test
        void getInvalidMovieFromOmdbAPI() {
            String invalidId = "tt404404";
            // Query OMDB API for new Movie
            OmdbDAO omdbDAO = new OmdbDAO();
            assertThrows(OmdbException.class, () -> omdbDAO.getMovieFromOmdbAPI(invalidId));
        }
    }

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