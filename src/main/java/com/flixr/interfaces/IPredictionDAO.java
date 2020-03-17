package com.flixr.interfaces;

import com.flixr.exceptions.EngineException;

/**
 * @author Thomas Thompson
 *
 * This a used for StandAlone testing, where the PredictionHarness can act as a DAO
 * This is so that the CSV can seem to mimic database actions
 */
public interface IPredictionDAO {

    public double getAveragePreferenceDifference(int movieId_i, int movieId_j) throws EngineException;

}
