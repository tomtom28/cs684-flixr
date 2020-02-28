package com.flixr.engine.utils;

/**
 * @author Thomas Thompson
 *
 * Reports errors back to calling methods of the Recommendation Engine
 *
 */
public class RecommendationEngineException extends Exception {
    public RecommendationEngineException(Exception e) {
        super(e);
    }
}
