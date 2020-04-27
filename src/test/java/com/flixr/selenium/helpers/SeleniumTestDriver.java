package com.flixr.selenium.helpers;

import com.flixr.exceptions.TestException;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.concurrent.TimeUnit;

import static com.flixr.selenium.helpers.SeleniumConstants.APP_URL;

public class SeleniumTestDriver {

    // Default Admin Credentials
    private String adminEmail = "admin@flixr.com";
    private String adminPassword = "admin";

    public String getAdminEmail() {
        return adminEmail;
    }
    public String getAdminPassword() {
        return adminPassword;
    }

    // Default User Credentials (over 18)
    private String defaultUserEmail = "jsmith@email.com";
    private String defaultUserPassword = "password";

    public String getDefaultUserEmail() {
        return defaultUserEmail;
    }
    public String getDefaultUserPassword() {
        return defaultUserPassword;
    }

    // Default Under-aged User Credentials (under 18)
    private String defaultUnder18UserEmail = "kid@email.com";
    private String defaultUnder18UserPassword = "password";

    public String getDefaultUnder18UserEmail() {
        return defaultUnder18UserEmail;
    }
    public String getDefaultUnder18UserPassword() {
        return defaultUnder18UserPassword;
    }

    /**
     * @author Thomas Thompson
     * Force the Test Driver to wait X number of seconds
     * @param seconds       Number of Seconds to Wait
     */
    public void waitXseconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {}
    }

    /**
     * @author Thomas Thompson
     * @param webDriver     Reference to Selenium Web Driver
     * @param userEmail     User Email Address
     * @param userPassword  User Password
     */
    public void loginUser(WebDriver webDriver, String userEmail, String userPassword) throws Exception {

        // Load Sign Up Page
        webDriver.get(APP_URL + "/signin");

        int DEBUG0 = 0;

        // Add Email
        WebElement emailForm = webDriver.findElement(By.id("inputEmail"));
        emailForm.sendKeys(userEmail);

        // Add Password
        WebElement passwordForm = webDriver.findElement(By.id("inputPassword"));
        passwordForm.sendKeys(userPassword);

        int DEBUG1 = 0;

        // Submit
        WebElement submitBtn = webDriver.findElement(By.className("btn-primary"));
        submitBtn.click();

        int DEBUG2 = 0;
    }


    /**
     * @author Thomas Thompson
     * Clicks the selected link (by name) in the navbar
     * @param webDriver     Reference to Selenium Web Driver
     * @param nameOfLink    Name Of Link in SideBar (ex. "Admin", or "Rating"
     */
    public void clickSideBarLinkByName(WebDriver webDriver, String nameOfLink) throws NoSuchElementException {
        // Click selected link in sidebar
        WebElement sideBar = webDriver.findElement(By.id("sidebar"));
        WebElement linkElement = ((RemoteWebElement) sideBar).findElementByLinkText(nameOfLink);
        linkElement.click();
    }


    /**
     * @author Thomas Thompson
     * Search a Movie on the Rating Page
     * @param webDriver         Reference to Selenium Web Driver
     * @param searchMovieName   Movie Name you want to search
     */
    public void searchForMovieToRateByName(WebDriver webDriver, String searchMovieName) {

        // Write Movie to search box
        WebElement searchBox = webDriver.findElement(By.name("search"));
        searchBox.sendKeys(searchMovieName);

        int DEBUG1 = 0;

        // Click Movie search button
        WebElement movieSearchBtn = webDriver.findElement(By.className("btn-light"));
        movieSearchBtn.click();

    }

    /**
     * @author Thomas Thompson
     * @param webDriver     Reference to Selenium Web Driver
     * @return  Returns the Name of the Movie currently shown in the Ratings Page
     */
    public String getCurrentMovieNameBeingRated(WebDriver webDriver) {
        // Movie Name (header) of tile in Rating page
        WebElement titleMovieName = webDriver.findElement(By.tagName("h3"));
        return titleMovieName.getText();
    }

    /**
     * @author Thomas Thompson
     * @param webDriver     Reference to Selenium Web Driver
     * @param numberOfStars Number of Stars to Rate Current Movie
     */
    public void rateCurrentMovie(WebDriver webDriver, int numberOfStars) throws TestException {

        // Ensure # of Stars is valid
        if (numberOfStars < 1 || numberOfStars > 5) {
            throw new TestException(new Exception("Invalid Number of Stars!"));
        }

        // Transform rating to star name
        int starNumber = 5 - numberOfStars + 1;
        String starName = "star" + starNumber;

        // Select the correct Star to give the right rating
        WebElement starBtn = webDriver.findElement(By.id(starName));
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].checked = true;", starBtn);

        // Submit the Rating
        this.waitXseconds(2); // wait 2 seconds
        WebElement submitBtn = webDriver.findElement(By.className("btn-primary"));
        submitBtn.click();

        int DEBUG = 0;
    }

    /**
     * Logs out the user by clicking the "log out" button
     * @author Thomas Thompson
     * @param webDriver     Reference to Selenium Web Driver
     */
    public void logOutUser(WebDriver webDriver) throws TestException {

        // Find & click the logout button
        WebElement logoutBtn = webDriver.findElement(By.linkText("Logout"));
        logoutBtn.click();

    }


}
