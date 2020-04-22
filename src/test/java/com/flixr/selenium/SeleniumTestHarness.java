package com.flixr.selenium;

import com.flixr.Application;
import com.flixr.selenium.helpers.SeleniumTestDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import static com.flixr.selenium.helpers.SeleniumConstants.APP_URL;
import static com.flixr.selenium.helpers.SeleniumConstants.WEB_DRIVER_LOCATION;
import static org.junit.Assert.fail;

/**
 * @author Thomas Thompson
 *
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
//    @AfterEach
//    void closeWebDriver() {
//        webDriver.quit(); // Closes chrome browser
//    }

    /**
     * @author Thomas Thompson
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to Load HomePage: " + e.getMessage());
            System.out.println("*** Ensure you are running the app.js in /frontend folder! ***");
        }
    }

    /**
     * @author Thomas Thompson
     * Verify that Admin User is able to see Admin Page
     */
    @Test
    void testAdminAccess() {

        // Admin Credentials
        String adminEmail = "admin@flixr.com";
        String adminPassword = "admin";

        try {
            // Open chrome browser
            WebDriver webDriver = new ChromeDriver();

            // Login User
            seleniumTestDriver.loginUser(webDriver, adminEmail, adminPassword);

            // Check that "Admin" exists in the sidebar
            WebElement sideBar = webDriver.findElement(By.id("sidebar"));
            WebElement adminLink = ((RemoteWebElement) sideBar).findElementByLinkText("Admin");

            // Navigate to "Admin" page
            adminLink.click();

        } catch(Exception e) {
            e.printStackTrace();
            fail("Unable to Load HomePage: " + e.getMessage());
        }
    }

}
