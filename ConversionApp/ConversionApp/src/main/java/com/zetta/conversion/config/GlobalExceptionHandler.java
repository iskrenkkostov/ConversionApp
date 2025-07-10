package com.zetta.conversion.config;

import com.zetta.conversion.exception.CurrencyLayerApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/**
 * Global exception handler for the application.
 * <p>
 * Handles various exceptions thrown during request processing and returns appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions thrown by the CurrencyLayer API.
     *
     * @param ex the {@link CurrencyLayerApiException} exception
     * @return a ResponseEntity with a descriptive error message and appropriate HTTP status
     */
    @ExceptionHandler(CurrencyLayerApiException.class)
    public ResponseEntity<String> handleCurrencyLayerApiException(CurrencyLayerApiException ex) {
        HttpStatus status;
        String msg;

        switch (ex.getCode()) {
            case 201:
                status = HttpStatus.BAD_REQUEST;
                msg = "Invalid source currency!";
                break;
            case 202:
                status = HttpStatus.BAD_REQUEST;
                msg = "Invalid target currency!";
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                msg = "CurrencyLayer API error " + ex.getCode() + ": " + ex.getInfo();
                break;
        }

        return ResponseEntity.status(status).body(msg);
    }

    /**
     * Handles exceptions where method arguments are of an unexpected type.
     *
     * @param ex the {@link MethodArgumentTypeMismatchException}
     * @return a ResponseEntity with a user-friendly error message
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("amount".equals(ex.getName())) {
            return ResponseEntity.badRequest().body("Amount must be a valid number.");
        }

        return ResponseEntity.badRequest().body("Invalid parameter: " + ex.getName());
    }

    /**
     * Handles {@link IllegalArgumentException} thrown during validation or processing.
     *
     * @param ex the exception
     * @return a ResponseEntity indicating a bad request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid input: " + ex.getMessage());
    }

    /**
     * Handles cases where required request parameters are missing.
     *
     * @param ex the {@link MissingServletRequestParameterException}
     * @return a ResponseEntity indicating missing input
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingRequiredParameter(MissingServletRequestParameterException ex) {
        if(ex.getParameterName().equals("fromCurrency")) {
            return ResponseEntity.badRequest().body("Invalid input: fromCurrency must be present.");
        }
        if(ex.getParameterName().equals("toCurrency")) {
            return ResponseEntity.badRequest().body("Invalid input: toCurrency must be present.");
        }
        if(ex.getParameterName().equals("amount")) {
            return ResponseEntity.badRequest().body("Invalid input: amount must be present.");
        }
        if(ex.getParameterName().equals("transactionId")) {
            return ResponseEntity.badRequest().body("Invalid input: transactionId must be present.");
        }
        if(ex.getParameterName().equals("transactionDateTime")) {
            return ResponseEntity.badRequest().body("Invalid input: transactionDateTime must be present.");
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid input: " + ex.getMessage());
    }

    /**
     * Handles {@link NoSuchElementException} when a requested element is not found.
     *
     * @param ex the exception
     * @return a ResponseEntity with 404 Not Found status
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleMissingRequiredParameter(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Invalid input: " + ex.getMessage());
    }

    /**
     * Handles 404 errors when a requested resource does not exist.
     *
     * @param ex the {@link NoResourceFoundException}
     * @return a ResponseEntity with 404 Not Found status
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleMissingRequiredParameter(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Invalid input: " + ex.getMessage());
    }
}
