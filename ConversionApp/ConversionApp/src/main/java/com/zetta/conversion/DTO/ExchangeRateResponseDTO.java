package com.zetta.conversion.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing the response received from the CurrencyLayer API for exchange rates.
 * This includes the success flag, source currency, conversion quotes, and any potential error details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned from the exchange rate of CurrencyLayer API")
public class ExchangeRateResponseDTO {

    /**
     * Indicates whether the API call was successful.
     */
    @Schema(description = "Indicates whether the API call was successful", example = "true")
    private boolean success;

    /**
     * The source currency used for the exchange rate calculations.
     * For CurrencyLayer, this is typically always "USD" unless configured otherwise.
     */
    @Schema(description = "The source currency for the exchange rates", example = "USD")
    private String source;

    /**
     * A map containing exchange rate quotes.
     * The key is a string representing the currency pair (e.g., "USDGBP"),
     * and the value is the exchange rate from the source currency to the target currency.
     */
    @Schema(
            description = "A map of currency pair quotes. The key is the currency pair (e.g., USDGBP), and the value is the exchange rate.",
            example = "{USDGBP: 0.76, USDEUR: 0.85}"
    )
    private Map<String, Double> quotes;

    /**
     * Object containing error details if the API call failed.
     * This is populated only when {@code success} is {@code false}.
     */
    @Schema(description = "Details of the error if the API call failed")
    private ErrorDTO error;
}
