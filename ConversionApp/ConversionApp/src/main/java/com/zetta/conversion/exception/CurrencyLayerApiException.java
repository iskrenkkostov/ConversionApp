package com.zetta.conversion.exception;

import lombok.Getter;

/**
 * Exception thrown when the CurrencyLayer API returns an error response.
 * <p>
 * Contains the error code and additional information provided by the API.
 * </p>
 */
@Getter
public class CurrencyLayerApiException extends RuntimeException {

    /**
     * The error code returned by the CurrencyLayer API.
     * -- GETTER --
     *  Returns the error code returned by the CurrencyLayer API.
     *
     * @return the API error code

     */
    private final int code;

    /**
     * Additional information or message related to the API error.
     * -- GETTER --
     *  Returns additional information or message related to the API error.
     *
     * @return the API error info message

     */
    private final String info;

    /**
     * Constructs a new {@code CurrencyLayerApiException} with the specified error code and info message.
     *
     * @param code the error code returned by the API
     * @param info additional information describing the error
     */
    public CurrencyLayerApiException(int code, String info) {
        super("CurrencyLayer API error " + code + ": " + info);
        this.code = code;
        this.info = info;
    }

}
