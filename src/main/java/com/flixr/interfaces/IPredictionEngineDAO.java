package com.flixr.interfaces;

/**
 * @author Thomas Thompson
 *
 * This a used for StandAlone testing, where the PredictionHarness can act as a DAO
 * This is so that the CSV can seem to mimic database actions
 */
public interface IPredictionEngineDAO {

    public double getAveragePreferenceDifference(int movieId_i, int movieId_j);

}
