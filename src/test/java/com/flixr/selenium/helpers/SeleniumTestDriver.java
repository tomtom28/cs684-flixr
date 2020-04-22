package com.flixr.selenium.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.flixr.selenium.helpers.SeleniumConstants.APP_URL;

public class SeleniumTestDriver {

    /**
     * @author Thomas Thompson
     * @param webDriver     Reference to Selenium Web Driver
     * @param userEmail     User Email Address
     * @param userPassword  User Password
     */
    public void loginUser(WebDriver webDriver, String userEmail, String userPassword) throws Exception {

        // Load Sign Up Page
        webDriver.get(APP_URL + "/signin");

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

}
