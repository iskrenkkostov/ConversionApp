package com.zetta.conversion.service;
import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.zetta.conversion.DTO.ConversionDetailsResponseDTO;
import com.zetta.conversion.DTO.ConversionResponseDTO;
import com.zetta.conversion.DTO.ExchangeRateResponseDTO;
import com.zetta.conversion.entity.ConversionTransaction;
import com.zetta.conversion.exception.CurrencyLayerApiException;
import com.zetta.conversion.repository.ConversionTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service class responsible for handling currency conversion operations
 * including exchange rate retrieval, conversion processing, and querying past transactions.
 */
@Service
public class ConversionService {
    private static final Logger logger = LoggerFactory.getLogger(ConversionService.class);

    @Value("${currency.api.key}")
    private String apiKey;

    @Value("${currency.api.url}")
    private String url;

    private final RestTemplate restTemplate;
    private final ConversionTransactionRepository conversionTransactionRepository;

    /**
     * Constructs a ConversionService with required dependencies.
     *
     * @param restTemplate                   the RestTemplate for HTTP requests
     * @param conversionTransactionRepository repository to manage ConversionTransaction entities
     */
    public ConversionService(RestTemplate restTemplate, ConversionTransactionRepository conversionTransactionRepository) {
        this.restTemplate = restTemplate;
        this.conversionTransactionRepository = conversionTransactionRepository;
    }

    /**
     * Retrieves the exchange rate between two currencies from the CurrencyLayer API.
     *
     * @param fromCurrency the source currency code (e.g., USD)
     * @param toCurrency   the target currency code (e.g., EUR)
     * @return the exchange rate as a Double
     * @throws CurrencyLayerApiException if the API returns an error
     * @throws RuntimeException          if the API call is unsuccessful
     */
    public Double getExchangeRate(String fromCurrency, String toCurrency) {
        if(fromCurrency.isBlank()) {
            throw new IllegalArgumentException("fromCurrency must be present");
        }

        if(toCurrency.isBlank()) {
            throw new IllegalArgumentException("toCurrency must be present");
        }

        logger.info("Fetching exchange rate from {} to {}", fromCurrency, toCurrency);
        String fullURL = url + "/live" + "?access_key=" + apiKey +
                "&source=" + fromCurrency +
                "&currencies=" + toCurrency +
                "&format=1";

        ResponseEntity<ExchangeRateResponseDTO> response = restTemplate.getForEntity(fullURL, ExchangeRateResponseDTO.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            if (!response.getBody().isSuccess()) {
                logger.error("CurrencyLayer API error: code={}, info={}",
                        response.getBody().getError().getCode(),
                        response.getBody().getError().getInfo());
                throw new CurrencyLayerApiException(response.getBody().getError().getCode(), response.getBody().getError().getInfo());
            }
            Double rate = response.getBody().getQuotes().get(fromCurrency + toCurrency);
            logger.debug("Exchange rate obtained: {}", rate);
            return rate;
        } else {
            logger.error("Failed to fetch exchange rate, status: {}", response.getStatusCode());
            throw new RuntimeException("Unsuccessful course retrieval! Status: " + response.getStatusCode());
        }
    }

    /**
     * Converts a specified amount from one currency to another and stores the transaction.
     *
     * @param amount       the amount to convert
     * @param fromCurrency the source currency code
     * @param toCurrency   the target currency code
     * @return a detailed DTO containing conversion results and transaction metadata
     * @throws IllegalArgumentException if the amount is not greater than 0
     */
    public ConversionDetailsResponseDTO getConvertedAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        logger.info("Converting amount {} from {} to {}", amount, fromCurrency, toCurrency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid amount for conversion: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Double rate = getExchangeRate(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate)).setScale(4, RoundingMode.HALF_UP);
        UUID transactionId = UUID.randomUUID();
        LocalDateTime transactionDateTime = LocalDateTime.now();

        ConversionTransaction conversionTransaction = ConversionTransaction.builder().convertedAmount(convertedAmount).transactionId(transactionId).
                dateTime(transactionDateTime).fromCurrency(fromCurrency).toCurrency(toCurrency).rate(rate).originalAmount(amount).build();

        conversionTransactionRepository.save(conversionTransaction);
        logger.info("Conversion transaction saved with ID {}", transactionId);

        return new ConversionDetailsResponseDTO(convertedAmount, transactionId, transactionDateTime, fromCurrency, toCurrency, rate, amount);
    }

    /**
     * Retrieves a single conversion by transaction ID.
     *
     * @param transactionId the UUID of the transaction
     * @return a {@link ConversionResponseDTO}
     * @throws NoSuchElementException if not found
     */
    public ConversionResponseDTO getConversionByTransactionId(UUID transactionId) {
        logger.info("Fetching conversion by transaction ID: {}", transactionId);
        ConversionTransaction transaction = conversionTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    logger.warn("Transaction not found for ID {}", transactionId);
                    return new NoSuchElementException("Transaction not found for the given ID");
                });

        return toConversionResponseDTO(transaction);
    }

    /**
     * Retrieves a paginated list of conversions by transaction date.
     *
     * @param transactionDateTime the date to filter
     * @param page                pagination page number
     * @param size                pagination size
     * @return a page of {@link ConversionResponseDTO}
     * @throws IllegalArgumentException if date is in the future
     * @throws NoSuchElementException if no transactions found
     */
    public Page<ConversionResponseDTO> getConversionsByTransactionDate(LocalDate transactionDateTime, int page, int size) {
        logger.info("Fetching conversions by transaction date: {}", transactionDateTime);

        if (transactionDateTime.isAfter(LocalDate.now())) {
            logger.warn("Transaction date {} is in the future", transactionDateTime);
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }

        LocalDateTime startOfDay = transactionDateTime.atStartOfDay();
        LocalDateTime endOfDay = transactionDateTime.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").descending());
        Page<ConversionTransaction> transactionsPage = conversionTransactionRepository.findByDateTimeBetween(startOfDay, endOfDay, pageable);

        if (!transactionsPage.hasContent()) {
            logger.warn("No transactions found for date {}", transactionDateTime);
            throw new NoSuchElementException("No transactions found for the given date");
        }

        return transactionsPage.map(this::toConversionResponseDTO);
    }

    /**
     * Maps a {@link ConversionTransaction} entity to a {@link ConversionResponseDTO}.
     *
     * @param entity the conversion transaction entity
     * @return a simplified DTO for API response
     */
    private ConversionResponseDTO toConversionResponseDTO(ConversionTransaction entity) {
        return new ConversionResponseDTO(entity.getConvertedAmount(), entity.getTransactionId());
    }
}
