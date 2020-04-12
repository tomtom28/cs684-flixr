package com.flixr.configuration;

/**
 * @author Green Team
 * Add any application constants here
 */
public class ApplicationConstants {

    // Database Credentials
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";
    public static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3307/flixr";

    // OMDB API Key
    public static final String OMDB_API_KEY = "9ba6dc15";

    // Engine Thread Counts
    public static final int PRED_ENGINE_THREADS = 4;
    public static final int REC_ENGINE_THREADS = 4;

    // Toggle Trained Model type (DB vs CSV)
    public static final boolean USE_CSV_MATRIX = true;
    public static final String CSV_MATRIX_FILE_PATH = "/src/main/resources/ml-models/";
    public static final String CSV_MATRIX_FILE_PREFIX = "model";

    // Max Admin Page Limit
    public static final int MAX_ADMIN_LIMIT = 50;
}
