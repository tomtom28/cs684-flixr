package com.flixr.utils;

import com.flixr.utils.helpers.RecommendationEngine.RecommendationEngineHarnessTestDriver;
import com.flixr.utils.helpers.RecommendationEngine.RecommendationEngineHarnessTestOracle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Thompson
 *
 * Unit Tests related to running the RecommendationEngine as a Stand Alone Sub-System
 *
 * Used to launch a Stand Alone version of the Recommendation Engine
 * Purpose is for backend testing / development
 */
public class RecommendationEngineTestHarness {

    private RecommendationEngineHarnessTestDriver recommendationEngineHarnessTestDriver;
    private RecommendationEngineHarnessTestOracle recommendationEngineHarnessTestOracle;

    // Create a new Oracle for Standalone testing
    @BeforeEach
    public void initEach() {
        recommendationEngineHarnessTestDriver = new RecommendationEngineHarnessTestDriver();
        recommendationEngineHarnessTestOracle = new RecommendationEngineHarnessTestOracle();
        recommendationEngineHarnessTestOracle.intialize(recommendationEngineHarnessTestDriver);
    }

    /**
     * Thomas Thompson, Test # 1
     * Test Name: Unit-1
     *
     * Ensure that RecommendationEngine Runs & Generates a Valid Correlation Matrix
     *
     * Heuristic Based Approach:
     * 1) Spot check that Matrix diagonal only has zeros
     * 2) Spot check that (i,j) and (j,i) have the same magnitude but are negatives of each other
     */
    @Test
    void testValidMatrixForRecommendationEngineHarness() {

        int numberOfMatrixDiagonalTests = 10;
        int numberOfMatrixSymmetryTests = 100;

        try {
            // Call Test Driver to generate input objects
            recommendationEngineHarnessTestDriver.readInputFile(recommendationEngineHarnessTestDriver.getDefaultInputFile());

            // Train Model & Generate Correlation Matrix
            recommendationEngineHarnessTestDriver.trainModel();

            // Load Trained Model (i.e. Correlation Matrix)
            recommendationEngineHarnessTestDriver.loadCorrelationMatrix();

            // Step 1 - Test N number of random positions along matrix diagonal
            recommendationEngineHarnessTestOracle.validateMatrixDiagonals(numberOfMatrixDiagonalTests);

            // Step 2 - Test N number of random mirrored positions (i,j) = -(j,i)
            recommendationEngineHarnessTestOracle.validateMatrixSymmetry(numberOfMatrixSymmetryTests);

        } catch (Exception e) {
            fail("Unable to complete run! Error was thrown: " + e.getMessage());
        }
    }


    /**
     * Thomas Thompson, Test # 2
     * Test Name: Unit-2
     *
     * Run RecommendationEngineHarness to assess run time performance
     * Test Oracle will Compare these run times using a trend line against the O(n) Time Complexity
     */
    @Test
    void testRunTimePerformanceForRecommendationEngineHarness() {
        try {

            // Iterate over all input files from Test Driver
            for (String fileName : recommendationEngineHarnessTestDriver.getInputFiles()) {

                // Call Test Driver to generate input objects
                recommendationEngineHarnessTestDriver.readInputFile(recommendationEngineHarnessTestDriver.getProjectPath() + fileName);

                // Train Model & Generate Correlation Matrix
                double runTimeInSeconds = recommendationEngineHarnessTestDriver.trainModel();

                // Record performance into Oracle
                recommendationEngineHarnessTestOracle.addRunTimePerformanceEntry(fileName, runTimeInSeconds);
            }

            // Calculate Input File Properties
            recommendationEngineHarnessTestOracle.analyzeInputFiles();

            // Save & Display Run Time Performance Results
            recommendationEngineHarnessTestOracle.printRunTimePerformanceResults();

        } catch (Exception e) {
            fail("Unable to complete run! Error was thrown: " + e.getMessage());
        }
    }

}