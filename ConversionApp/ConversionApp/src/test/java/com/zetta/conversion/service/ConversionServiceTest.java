package com.zetta.conversion.service;

import com.zetta.conversion.DTO.ConversionDetailsResponseDTO;
import com.zetta.conversion.DTO.ConversionResponseDTO;
import com.zetta.conversion.DTO.ErrorDTO;
import com.zetta.conversion.DTO.ExchangeRateResponseDTO;
import com.zetta.conversion.entity.ConversionTransaction;
import com.zetta.conversion.exception.CurrencyLayerApiException;
import com.zetta.conversion.repository.ConversionTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConversionTransactionRepository repository;

    @InjectMocks
    private ConversionService conversionService;

    @Test
    void getExchangeRate_apiReturnsAsExpected() {
        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        Double result = conversionService.getExchangeRate("USD", "BGN");
        Double expectedValue = 1.80;

        assertNotNull(result);
        assertEquals(expectedValue, result);
    }

    @Test
    void getExchangeRate_whenFromCurrencyIsBlank_thenThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.getExchangeRate("   ", "USD");
        });
        assertEquals("fromCurrency must be present", exception.getMessage());
    }

    @Test
    void getExchangeRate_whenToCurrencyIsBlank_thenThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.getExchangeRate("USD", " ");
        });
        assertEquals("toCurrency must be present", exception.getMessage());
    }

    @Test
    void getExchangeRate_apiReturnsNon2xxStatus_throwsRuntimeException() {
        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                conversionService.getExchangeRate("USD", "BGN")
        );

        assertTrue(exception.getMessage().contains("Unsuccessful course retrieval! Status:"));
    }

    @Test
    void getExchangeRate_nullResponseBody_throwsRuntimeException() {
        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                conversionService.getExchangeRate("USD", "BGN")
        );

        assertTrue(exception.getMessage().contains("Unsuccessful course retrieval! Status:"));
    }

    @Test
    void getExchangeRate_apiSuccessFalse_throwsCurrencyLayerApiException() {
        ErrorDTO error = new ErrorDTO();
        error.setCode(101);
        error.setInfo("Invalid API key");

        ExchangeRateResponseDTO responseBody = new ExchangeRateResponseDTO();
        responseBody.setSuccess(false);
        responseBody.setError(error);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        CurrencyLayerApiException exception = assertThrows(CurrencyLayerApiException.class, () ->
                conversionService.getExchangeRate("USD", "BGN")
        );

        assertEquals(101, exception.getCode());
        assertEquals("CurrencyLayer API error 101: Invalid API key", exception.getMessage());
    }

    @Test
    void getExchangeRate_quoteMissing_returnsNull() {
        ExchangeRateResponseDTO responseBody = new ExchangeRateResponseDTO();
        responseBody.setSuccess(true);
        responseBody.setSource("USD");
        responseBody.setQuotes(Map.of());  // Empty map

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        Double rate = conversionService.getExchangeRate("USD", "BGN");

        assertNull(rate);  // or expect a specific exception if you want to guard against it
    }

    @Test
    void getExchangeRate_nullFromCurrency_throwsException() {
        assertThrows(NullPointerException.class, () ->
                conversionService.getExchangeRate(null, "BGN")
        );
    }

    @Test
    void getConvertedAmount_validInput_returnsExpected() {
        BigDecimal amount = BigDecimal.valueOf(100);
        String from = "USD";
        String to = "BGN";
        Double mockRate = 1.8;

        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        when(repository.save(any(ConversionTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversionDetailsResponseDTO result = conversionService.getConvertedAmount(amount, from, to);

        assertNotNull(result);
        assertEquals(amount, result.getOriginalAmount());
        assertEquals(from, result.getFromCurrency());
        assertEquals(to, result.getToCurrency());
        assertEquals(mockRate, result.getRate());
        assertEquals(amount.multiply(BigDecimal.valueOf(mockRate)).setScale(4, RoundingMode.HALF_UP), result.getConvertedAmount());
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getDateTime());
    }

    @Test
    void getConvertedAmount_zeroAmount_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                conversionService.getConvertedAmount(BigDecimal.ZERO, "USD", "BGN")
        );

        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void getConvertedAmount_negativeAmount_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                conversionService.getConvertedAmount(BigDecimal.valueOf(-50), "USD", "BGN")
        );

        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void getConvertedAmount_exchangeRateFails_throwsException() {

        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        when(conversionService.getExchangeRate("USD", "BGN"))
                .thenThrow(new CurrencyLayerApiException(101, "Invalid API key"));

        CurrencyLayerApiException exception = assertThrows(CurrencyLayerApiException.class, () ->
                conversionService.getConvertedAmount(BigDecimal.valueOf(100), "USD", "BGN")
        );

        assertEquals(101, exception.getCode());
        assertEquals("CurrencyLayer API error 101: Invalid API key", exception.getMessage());
    }

    @Test
    void getConvertedAmount_exchangeRateIsNull_throwsNullPointerException() {

        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        when(conversionService.getExchangeRate("USD", "BGN")).thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                conversionService.getConvertedAmount(BigDecimal.valueOf(100), "USD", "BGN")
        );
    }

    @Test
    void getConvertedAmount_repositoryFails_throwsRuntimeException() {
        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ExchangeRateResponseDTO.class)))
                .thenReturn(response);

        when(repository.save(any()))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                conversionService.getConvertedAmount(BigDecimal.valueOf(100), "USD", "BGN")
        );

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void getConversionsByTransactionId_validId_returnsDTO() {
        UUID transactionId = UUID.randomUUID();
        BigDecimal convertedAmount = BigDecimal.valueOf(123.45);
        ConversionTransaction transaction = new ConversionTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setConvertedAmount(convertedAmount);

        when(repository.findByTransactionId(transactionId)).thenReturn(Optional.of(transaction));

        ConversionResponseDTO result = conversionService.getConversionByTransactionId(transactionId);

        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(convertedAmount, result.getConvertedAmount());
    }

    @Test
    void getConversionsByDate_validDate_returnsPageOfDTOs() {
        LocalDate date = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(0, 3, Sort.by("dateTime").descending());

        ConversionTransaction transaction1 = new ConversionTransaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setConvertedAmount(BigDecimal.valueOf(99.99));

        ConversionTransaction transaction2 = new ConversionTransaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setConvertedAmount(BigDecimal.valueOf(99.99));

        ConversionTransaction transaction3 = new ConversionTransaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setConvertedAmount(BigDecimal.valueOf(99.99));

        Page<ConversionTransaction> page = new PageImpl<>(List.of(transaction1, transaction2, transaction3));
        when(repository.findByDateTimeBetween(startOfDay, endOfDay, pageable)).thenReturn(page);

        Page<ConversionResponseDTO> result = conversionService.getConversionsByTransactionDate(date, 0, 3);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(transaction1.getTransactionId(), result.getContent().getFirst().getTransactionId());
        assertEquals(transaction2.getTransactionId(), result.getContent().get(1).getTransactionId());
        assertEquals(transaction3.getTransactionId(), result.getContent().getLast().getTransactionId());
    }

    @Test
    void getConversionsByTransactionId_transactionIdNotFound_throwsNoSuchElementException() {
        UUID transactionId = UUID.randomUUID();
        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                conversionService.getConversionByTransactionId(transactionId)
        );

        assertEquals("Transaction not found for the given ID", ex.getMessage());
    }

    @Test
    void getConversionsByTransactionDateTime_futureDate_throwsIllegalArgumentException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                conversionService.getConversionsByTransactionDate(futureDate, 0, 10)
        );

        assertEquals("Transaction date cannot be in the future", ex.getMessage());
    }

    @Test
    void getConversionsByTransactionDateTime_noDataFound_throwsNoSuchElementException() {
        LocalDate date = LocalDate.now().minusDays(1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dateTime").descending());
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        when(repository.findByDateTimeBetween(startOfDay, endOfDay, pageable))
                .thenReturn(Page.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                conversionService.getConversionsByTransactionDate(date, 0, 10)
        );

        assertEquals("No transactions found for the given date", ex.getMessage());
    }
}