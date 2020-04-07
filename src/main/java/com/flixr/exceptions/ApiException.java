package com.flixr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Thomas Thompson
 *
 * Exception Handling to Front End
 */

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Run Time Error")
public class ApiException extends Exception {
    public ApiException() {
        super();
    }
}
