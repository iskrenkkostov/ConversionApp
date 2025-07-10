package com.zetta.conversion.config;

import com.zetta.conversion.controller.ConversionController;
import com.zetta.conversion.exception.CurrencyLayerApiException;
import com.zetta.conversion.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ConversionController conversionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(conversionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenCurrencyLayerApiException_thenReturnsBadRequestForInvalidFromCurrency() throws Exception {
        when(conversionService.getExchangeRate("XXX", "BGN"))
                .thenThrow(new CurrencyLayerApiException(201, "Invalid source currency"));

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", "XXX")
                        .param("toCurrency", "BGN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid source currency!"));
    }

    @Test
    void whenCurrencyLayerApiException_thenReturnsBadRequestForInvalidToCurrency() throws Exception {
        when(conversionService.getExchangeRate("BGN", "XXX"))
                .thenThrow(new CurrencyLayerApiException(202, "Invalid target currency"));

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", "BGN")
                        .param("toCurrency", "XXX"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid target currency!"));
    }

    @Test
    void whenCurrencyLayerApiException_thenReturnsInternalServerError() throws Exception {
        when(conversionService.getExchangeRate(anyString(), anyString()))
                .thenThrow(new CurrencyLayerApiException(500, "CurrencyLayer API error"));

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", anyString())
                        .param("toCurrency", anyString()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("CurrencyLayer API error 500: CurrencyLayer API error"));
    }

    @Test
    void whenInvalidAmountType_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("amount", "invalid") // triggers type mismatch
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "BGN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount must be a valid number."));
    }


    @Test
    void whenMissingParam_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/exchange-rate") // missing `fromCurrency`
                        .param("toCurrency", "BGN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid input: fromCurrency must be present.")));
    }

    @Test
    void whenIllegalArgument_thenReturnsBadRequest() throws Exception {
        when(conversionService.getExchangeRate("USD", "INVALID"))
                .thenThrow(new IllegalArgumentException("Target currency is not supported."));

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: Target currency is not supported."));
    }

    @Test
    void whenNoSuchElement_thenReturnsNotFound() throws Exception {
        when(conversionService.getExchangeRate("USD", "XXX"))
                .thenThrow(new NoSuchElementException("Currency not found."));

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "XXX"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid input: Currency not found."));
    }

    @Test
    void whenMissingFromCurrency_thenBadRequestWithCustomMessage() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("amount", "100")
                        .param("toCurrency", "USD"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: fromCurrency must be present."));
    }

    // Test missing 'toCurrency' param
    @Test
    void whenMissingToCurrency_thenBadRequestWithCustomMessage() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("amount", "100")
                        .param("fromCurrency", "EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: toCurrency must be present."));
    }

    // Test missing 'amount' param
    @Test
    void whenMissingAmount_thenBadRequestWithCustomMessage() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("fromCurrency", "EUR")
                        .param("toCurrency", "USD"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: amount must be present."));
    }

    // Test missing 'transactionId' param
    @Test
    void whenMissingTransactionId_thenBadRequestWithCustomMessage() throws Exception {
        mockMvc.perform(get("/api/conversions/by-id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: transactionId must be present."));
    }

    // Test missing 'transactionDateTime' param
    @Test
    void whenMissingTransactionDateTime_thenBadRequestWithCustomMessage() throws Exception {
        mockMvc.perform(get("/api/conversions/by-date"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: transactionDateTime must be present."));
    }

}