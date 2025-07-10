package com.zetta.conversion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a currency conversion transaction.
 *
 * <p>This entity is mapped to the "conversion" table in the database and stores
 * details of a currency conversion such as the original amount, converted amount,
 * currency pair, exchange rate, transaction timestamp, and a unique transaction ID.</p>
 *
 * <p>The primary key is an auto-generated long ID for internal use, while a UUID
 * transaction ID is also generated for external/public reference.</p>
 */
@Entity
@Table(name = "conversion")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionTransaction {
    /**
     * Internal database-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The converted amount after applying the exchange rate.
     */
    private BigDecimal convertedAmount;

    /**
     * A unique identifier for this transaction, used for public-facing operations.
     */
    private UUID transactionId;

    /**
     * The timestamp when the conversion took place.
     */
    private LocalDateTime dateTime;

    /**
     * The currency code of the source currency (e.g., "USD").
     */
    private String fromCurrency;

    /**
     * The currency code of the target currency (e.g., "EUR").
     */
    private String toCurrency;

    /**
     * The exchange rate applied during the conversion.
     */
    private Double rate;

    /**
     * The original amount before conversion.
     */
    private BigDecimal originalAmount;
}
