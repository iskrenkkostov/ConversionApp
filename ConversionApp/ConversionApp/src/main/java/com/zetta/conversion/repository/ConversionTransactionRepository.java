package com.zetta.conversion.repository;

import com.zetta.conversion.entity.ConversionTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for performing CRUD and custom operations
 * on {@link ConversionTransaction} entities.
 *
 * <p>This interface extends {@link JpaRepository}, providing standard
 * methods for persistence and allowing custom query method definitions.</p>
 */
public interface ConversionTransactionRepository extends JpaRepository<ConversionTransaction, Long> {

    /**
     * Finds a single conversion transaction that matches the given transaction ID.
     *
     * @param transactionId the UUID of the transaction to search for
     * @return a matching {@link ConversionTransaction}, or null if not found
     */
    Optional<ConversionTransaction> findByTransactionId(UUID transactionId);


    /**
     * Finds all conversion transactions that occurred between the specified
     * start and end timestamps.
     *
     * @param startOfDay the beginning of the date range
     * @param endOfDay   the end of the date range
     * @param pageable   pagination information
     * @return a page of {@link ConversionTransaction} records within the date range
     */
    Page<ConversionTransaction> findByDateTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable);
}
