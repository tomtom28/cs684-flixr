package com.flixr.dao;

import com.flixr.beans.UserSubmission;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Thomas Thompson
 */
public class EngineDAOTest {

    @Test
    void testGetUserSubmission() {

        // Test UserId 1
        int userId = 1;

        // Test DB Connection
        try {
            // Get UserSubmission from DAO
            EngineDAO engineDAO = new EngineDAO();
            UserSubmission userSubmission = engineDAO.getUserSubmission(userId);

            // Total Count Test
            assertEquals(232, userSubmission.getMoviesViewed().size(), "Total Count of Movies Rated should match!");

        } catch (SQLException e) {
            fail("Unable to query database: " + e.getMessage());
        }
    }


}
