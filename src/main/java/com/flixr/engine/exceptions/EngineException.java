package com.flixr.engine.exceptions;

/**
 * @author Thomas Thompson
 *
 * Reports errors back to calling methods of the Recommendation Engine
 *
 */
public class RecommendationEngineException extends Exception {

    private String engineMessage = "Unknown error during training of the Recommendation Engine.";

    public RecommendationEngineException(Exception e) {
        super(e);
    }

    public void setEngineMessage(String engineMessage) {
        this.engineMessage = engineMessage;
    }

    public String getEngineMessage() {
        return engineMessage;
    }
}
