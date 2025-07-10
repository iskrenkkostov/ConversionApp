package com.zetta.conversion.controller;

import com.zetta.conversion.DTO.ConversionDetailsResponseDTO;
import com.zetta.conversion.DTO.ConversionResponseDTO;
import com.zetta.conversion.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for currency conversion operations.
 * <p>
 * Provides endpoints to retrieve exchange rates, perform currency conversions,
 * and query past conversion transactions by transaction ID or date.
 */
@RestController
@RequestMapping("/api")
public class ConversionController {
    private static final Logger logger = LoggerFactory.getLogger(ConversionController.class);

    private final ConversionService conversionService;

    /**
     * Constructs a new ConversionController with the given service.
     *
     * @param conversionService the conversion service to delegate business logic to
     */
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Retrieves the live exchange rate between two currencies using the CurrencyLayer API.
     *
     * @param fromCurrency the source currency code (e.g., "USD")
     * @param toCurrency   the target currency code (e.g., "EUR")
     * @return the current exchange rate as a Double wrapped in ResponseEntity
     */
    @Operation(
            summary = "Get live exchange rate",
            description = "Retrieves the exchange rate between two currencies using the CurrencyLayer API.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/exchange-rate")
    public ResponseEntity<Double> getExchangeRate(
            @Parameter(description = "Source currency code (e.g., USD)", required = true) @RequestParam String fromCurrency,
            @Parameter(description = "Target currency code (e.g., EUR)", required = true) @RequestParam String toCurrency) {

        logger.info("API call: getExchangeRate from {} to {}", fromCurrency, toCurrency);
        return ResponseEntity.ok(conversionService.getExchangeRate(fromCurrency, toCurrency));
    }

    /**
     * Converts a specified amount from one currency to another.
     * Returns the converted amount and the unique transaction ID.
     *
     * @param amount       the amount to convert
     * @param fromCurrency the source currency code
     * @param toCurrency   the target currency code
     * @return a ResponseEntity containing ConversionResponseDTO with conversion result
     */
    @Operation(
            summary = "Convert amount between currencies",
            description = "Converts an amount from one currency to another and returns it and the transaction ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Conversion completed"),
                    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., negative amount)"),
                    @ApiResponse(responseCode = "500", description = "CurrencyLayer API error or internal error")
            }
    )
    @GetMapping("/convert")
    public ResponseEntity<ConversionResponseDTO> getConvertedAmount(
            @Parameter(description = "Amount to convert", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "Source currency code", required = true) @RequestParam String fromCurrency,
            @Parameter(description = "Target currency code", required = true) @RequestParam String toCurrency) {

        logger.info("API call: getConvertedAmount amount={} from {} to {}", amount, fromCurrency, toCurrency);
        ConversionDetailsResponseDTO detailedResponse = conversionService.getConvertedAmount(amount, fromCurrency, toCurrency);

        ConversionResponseDTO response = new ConversionResponseDTO(
                detailedResponse.getConvertedAmount(),
                detailedResponse.getTransactionId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a conversion by its unique transaction ID.
     *
     * @param transactionId the transaction ID to search for
     * @return a {@link ConversionResponseDTO} if found
     */
    @Operation(
            summary = "Get conversion by transaction ID",
            description = "Returns a single currency conversion by its unique transaction ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Conversion found"),
                    @ApiResponse(responseCode = "404", description = "Conversion not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid transaction ID")
            }
    )
    @GetMapping("/conversions/by-id")
    public ResponseEntity<ConversionResponseDTO> getConversionByTransactionId(
            @Parameter(description = "Transaction ID", required = true)
            @RequestParam UUID transactionId) {

        logger.info("API call: getConversionByTransactionId id={}", transactionId);
        ConversionResponseDTO result = conversionService.getConversionByTransactionId(transactionId);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves conversions filtered by date with pagination.
     *
     * @param transactionDateTime the date of the transactions (ISO format yyyy-MM-dd)
     * @param page                the page number (default is 0)
     * @param size                the page size (default is 3)
     * @return a paginated list of conversions matching the date
     */
    @Operation(
            summary = "Get conversions by transaction date",
            description = "Returns a paginated list of conversions filtered by date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Conversions found"),
                    @ApiResponse(responseCode = "400", description = "Invalid date"),
                    @ApiResponse(responseCode = "404", description = "No conversions found")
            }
    )
    @GetMapping("/conversions/by-date")
    public ResponseEntity<Page<ConversionResponseDTO>> getConversionsByTransactionDate(
            @Parameter(description = "Transaction date (yyyy-MM-dd)", example = "2025-07-08")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDateTime,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "3") @RequestParam(defaultValue = "3") int size) {

        logger.info("API call: getConversionByTransactionDate date={}, page={}, size={}", transactionDateTime, page, size);
        Page<ConversionResponseDTO> result = conversionService.getConversionsByTransactionDate(transactionDateTime, page, size);
        return ResponseEntity.ok(result);
    }
}
