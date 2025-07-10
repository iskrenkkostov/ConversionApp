package com.zetta.conversion.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing error details returned by the CurrencyLayer API.
 * Contains the HTTP status code and descriptive error information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details about the error returned by the CurrencyLayer API")
public class ErrorDTO {

    /**
     * HTTP status code of the error.
     */
    @Schema(description = "HTTP status code of the error", example = "400")
    private int code;

    /**
     * Detailed information about the error.
     */
    @Schema(description = "Detailed information about the error", example = "Invalid currency code")
    private String info;
}
