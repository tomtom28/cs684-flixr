package com.flixr.selenium;

import com.flixr.Application;
import com.flixr.selenium.helpers.SeleniumTestDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import static com.flixr.selenium.helpers.SeleniumConstants.APP_URL;
import static com.flixr.selenium.helpers.SeleniumConstants.WEB_DRIVER_LOCATION;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Selenium Test Harness for Full System Testing
 *
 * NOTE:
 * Must be using a Windows system
 * Assumes that Chrome web browser will be used
 *
 * Setup instructions found here:
 * https://link.medium.com/7fEYAcKFS5
 *
 */
public class SeleniumTestHarness {

    private SeleniumTestDriver seleniumTestDriver;

    @BeforeAll
    static void initializeAPI() {
        Application.main(new String[]{});
    }
    @BeforeAll
    static void intializeWebDriver() {
        String pathName = System.getProperty("user.dir");
        String chromeWebDriver = WEB_DRIVER_LOCATION;
        System.setProperty("webdriver.chrome.driver", pathName + chromeWebDriver);
    }
    @BeforeEach
    void initalize() {
        seleniumTestDriver = new SeleniumTestDriver();
    }


    /**
     * @author Thomas Thompson
     * Test ID: 21
     * Test Type: System
     * Test Name: TT-Selenium-1
     *
     * Verify that homepage can load
     */
    @Test
    void testHomePage() {
        try {

            // Open chrome browser
            WebDriver webDriver = new ChromeDriver();

            // Load Home Page
            webDriver.get(APP_URL);

            // Ensure that Title exists
            webDriver.findElement(By.className("title"));

            // Close chrome browser
            webDriver.quit();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to Load HomePage: Ensure you are running the app.js in the /frontend folder!");
        }
    }


    /**
     * @author Thomas Thompson
     * Test ID: 22
     * Test Type: System
     * Test Name: TT-Selenium-2
     *
     * Verify that the Admin User is able to see link to Admin Page
     */
    @Test
    void testAdminCanAccessAdminPage() {

        // Admin Credentials
        String adminEmail = seleniumTestDriver.getAdminEmail();
        String adminPassword = seleniumTestDriver.getAdminPassword();

        try {
            // Open chrome browser
            WebDriver webDriver = new ChromeDriver();

            // Login User
            seleniumTestDriver.loginUser(webDriver, adminEmail, adminPassword);

            // Click to "Admin" page
            seleniumTestDriver.clickSideBarLinkByName(webDriver, "Admin");

            // Close chrome browser
            webDriver.quit();

        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * @author Thomas Thompson
     * Test ID: 23
     * Test Type: System
     * Test Name: TT-Selenium-3
     *
     * Verify that non-Admin User is NOT able to see link to Admin Page
     */
    @Test
    void testNonAdminUserCannotAccessAdminPage() {

        // User Credentials
        String userEmail = seleniumTestDriver.getDefaultUserEmail();
        String userPassword = seleniumTestDriver.getDefaultUserPassword();

        try {
            // Open chrome browser
            WebDriver webDriver = new ChromeDriver();

            // Login User
            seleniumTestDriver.loginUser(webDriver, userEmail, userPassword);

            // Check that "Admin" does NOT exist in the sidebar
            assertThrows(NoSuchElementException.class, () ->  seleniumTestDriver.clickSideBarLinkByName(webDriver, "Admin"));

            // Close chrome browser
            webDriver.quit();

        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }


    /**
     * @author Thomas Thompson
     * Test ID: 24
     * Test Type: System
     * Test Name: TT-Selenium-4
     *
     * Verify that a user is able to search for a given movie title on the Rating Page search box
     */
    @Test
    void testMovieRatingSearchBox() {

        // User Credentials
        String userEmail = seleniumTestDriver.getDefaultUserEmail();
        String userPassword = seleniumTestDriver.getDefaultUserPassword();

        // Search Movie Title
        String movieName = "The Magnificent Seven";

        try {
            // Open chrome browser
            WebDriver webDriver = new ChromeDriver();

            // Login User
            seleniumTestDriver.loginUser(webDriver, userEmail, userPassword);

            // Click on Ratings
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            seleniumTestDriver.clickSideBarLinkByName(webDriver, "Rating");

            // Search for "Magnificent Seven" Movie
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            seleniumTestDriver.searchForMovieToRateByName(webDriver, movieName);

            // Check if Movie Name Appeared
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            String currentMovieTitleToRate = seleniumTestDriver.getCurrentMovieNameBeingRated(webDriver);
            assertEquals(movieName, currentMovieTitleToRate);

            // Close chrome browser
            webDriver.quit();

        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }


    /**
     * @author Thomas Thompson
     * Test ID: 25
     * Test Type: System
     * Test Name: TT-Selenium-5
     *
     * Verify that an older user is able to search for a given R-rated movie title
     * Verify that a younger user is NOT able to search for the same movie title (i.e. child blocks)
     */
    @Test
    void testUnderAgedUserCannotAccessRestrictedMovieInSearchBox() {

        // Adult User Credentials
        String olderUserEmail = seleniumTestDriver.getDefaultUserEmail();
        String olderUserPassword = seleniumTestDriver.getDefaultUserPassword();

        // Young User Credentials
        String youngUserEmail = seleniumTestDriver.getDefaultUnder18UserEmail();
        String youngUserPassword = seleniumTestDriver.getDefaultUnder18UserPassword();

        // Search Movie Title
        String movieName = "Superbad"; // known R-rated movie in database

        try {
            // Open chrome browsers (1 per user)
            WebDriver webDriverForOlderUser = new ChromeDriver();
            WebDriver webDriverForYoungerUser = new ChromeDriver();

            // Login both Users
            seleniumTestDriver.loginUser(webDriverForYoungerUser, youngUserEmail, youngUserPassword);
            seleniumTestDriver.loginUser(webDriverForOlderUser, olderUserEmail, olderUserPassword);

            // Click on Ratings for both users
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            seleniumTestDriver.clickSideBarLinkByName(webDriverForYoungerUser, "Rating");
            seleniumTestDriver.clickSideBarLinkByName(webDriverForOlderUser, "Rating");

            // Search for Movie for both users
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            seleniumTestDriver.searchForMovieToRateByName(webDriverForOlderUser, movieName);
            seleniumTestDriver.searchForMovieToRateByName(webDriverForYoungerUser, movieName);

            // Movie name should appear for Older User
            String currentMovieTitleOfOlderUser = seleniumTestDriver.getCurrentMovieNameBeingRated(webDriverForOlderUser);
            assertEquals(movieName, currentMovieTitleOfOlderUser); // YES equals for older user

            // Movie name should NOT appear for Young User
            seleniumTestDriver.waitXseconds(3); // wait 3 seconds
            String currentMovieTitleOfYoungUser = seleniumTestDriver.getCurrentMovieNameBeingRated(webDriverForYoungerUser);
            assertNotEquals(movieName, currentMovieTitleOfYoungUser); // NOT equals for younger user

            // Log out BOTH users
            seleniumTestDriver.logOutUser(webDriverForOlderUser);
            seleniumTestDriver.logOutUser(webDriverForYoungerUser);

            // Close BOTH chrome browsers
            webDriverForOlderUser.quit();
            webDriverForYoungerUser.quit();

        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }



    // TODO ... KEEP ADDING TESTS...

    // EXAMPLE: YOU CAN USE rateCurrentMovie() method from SeleniumTestDriver

    // Example: You can test other pages like "About" or "Recommend" using:
    // seleniumTestDriver.clickSideBarLinkByName(webDriver, "INSERT LINK NAME HERE");

    // TIP:
    // User  seleniumTestDriver.waitXseconds(3); to wait for 3 (or more) seconds for webpages to load

    // NOTE:
    // You MUST be running "node app.js" from "/frontend" folder BEFORE you can test full system


}
