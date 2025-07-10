package com.zetta.conversion.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;/**
 * Response DTO containing essentials of a currency conversion transaction.
 * Provides the converted amount and unique transaction identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing essentials of a currency conversion transaction")
public class ConversionResponseDTO {

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
}
