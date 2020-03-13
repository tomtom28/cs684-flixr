package com.flixr.exceptions;

/**
 * @author Thomas Thompson
 *
 * Reports errors back to calling methods of the IMDB DAO
 * This should signal if there is an issue querying the 3rd party API
 */

public class OmdbException extends Exception {
    public OmdbException(Exception e) {
        super(e);
    }
}
