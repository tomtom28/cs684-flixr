package com.flixr.exceptions;

/**
 * @author Thomas Thompson
 *
 * Reports errors back to calling methods of the Recommendation Engine
 *
 */
public class EngineException extends Exception {

    private String engineMessage = "Unknown error during training of the Recommendation Engine.";

    public EngineException(Exception e) {
        super(e);
    }

    public void setEngineMessage(String engineMessage) {
        this.engineMessage = engineMessage;
    }

    public String getEngineMessage() {
        return engineMessage;
    }
}
