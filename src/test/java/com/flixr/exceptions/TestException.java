package com.flixr.exceptions;

/**
 * @author Thomas Thompson
 * Exception thrown during testing, often used to skip invalid test conditions
 */
public class TestException extends Exception {
    private boolean isSkippedTest;
    public TestException (Exception e) {
        super(e);
        this.isSkippedTest = false;
    }
    public TestException (Exception e, boolean skipTest) {
        super(e);
        this.isSkippedTest = skipTest;
    }
    public boolean isSkippedTest() {
        return isSkippedTest;
    }
}
