package com.zetta.conversion.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO containing details of a currency conversion transaction.
 * Includes information such as the original and converted amounts,
 * conversion rate, transaction ID, currencies involved, and timestamp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing details of a currency conversion transaction")
public class ConversionDetailsResponseDTO {

    /**
     * The converted amount in the target currency.
     */
    @Schema(description = "The converted amount in the target currency", example = "134.50")
    private BigDecimal convertedAmount;

    /**
     * Unique identifier for the transaction.
     */
    @Schema(description = "Unique identifier for the transaction.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID transactionId;

    /**
     * Date and time when the conversion was made.
     */
    @Schema(description = "Date and time when the conversion was made", example = "2025-07-08T14:45:00")
    private LocalDateTime dateTime;

    /**
     * Currency code from which the amount was converted.
     */
    @Schema(description = "Currency code being converted from", example = "USD")
    private String fromCurrency;

    /**
     * Currency code to which the amount was converted.
     */
    @Schema(description = "Currency code being converted to", example = "EUR")
    private String toCurrency;

    /**
     * Conversion rate used for the transaction.
     */
    @Schema(description = "Conversion rate used in the transaction", example = "0.85")
    private Double rate;

    /**
     * Original amount before conversion.
     */
    @Schema(description = "Original amount before conversion", example = "158.24")
    private BigDecimal originalAmount;
}
