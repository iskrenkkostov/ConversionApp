package com.zetta.conversion.controller;

import com.zetta.conversion.DTO.ConversionDetailsResponseDTO;
import com.zetta.conversion.DTO.ConversionResponseDTO;
import com.zetta.conversion.DTO.ExchangeRateResponseDTO;
import com.zetta.conversion.repository.ConversionTransactionRepository;
import com.zetta.conversion.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConversionControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConversionTransactionRepository repository;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ConversionController conversionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(conversionController).build();
    }

    @Test
    void getExchangeRate_returnsRate() throws Exception {
        when(conversionService.getExchangeRate("USD", "BGN")).thenReturn(1.8);

        mockMvc.perform(get("/api/exchange-rate")
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "BGN"))
                .andExpect(status().isOk())
                .andExpect(content().string("1.8"));
    }

    @Test
    void getConvertedAmount_returnsResponseDTO() throws Exception {
        UUID transactionId = UUID.randomUUID();
        BigDecimal converted = new BigDecimal("180.00");

        ConversionDetailsResponseDTO mockDetailedResponse = new ConversionDetailsResponseDTO(converted, transactionId, LocalDateTime.now(),
                "USD", "BGN", 1.80, BigDecimal.valueOf(100));

        ExchangeRateResponseDTO mockResponse = new ExchangeRateResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setSource("USD");
        mockResponse.setQuotes(Map.of("USDBGN", 1.80));
        mockResponse.setError(null);

        ResponseEntity<ExchangeRateResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(conversionService.getConvertedAmount(BigDecimal.valueOf(100), "USD", "BGN"))
                .thenReturn(mockDetailedResponse);

        mockMvc.perform(get("/api/convert")
                        .param("amount", "100")
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "BGN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value("180.0"))
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()));
    }

    @Test
    void getConversionsByDate_returnsPage() throws Exception {
        UUID transactionId = UUID.randomUUID();
        LocalDate date = LocalDate.now().minusDays(1);

        ConversionResponseDTO dto = new ConversionResponseDTO(BigDecimal.valueOf(180.00), transactionId);
        Page<ConversionResponseDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 3), 1);

        when(conversionService.getConversionsByTransactionDate(date, 0, 3)).thenReturn(page);

        mockMvc.perform(get("/api/conversions/by-date").param("transactionDateTime", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].convertedAmount").value("180.0"))
                .andExpect(jsonPath("$.content[0].transactionId").value(transactionId.toString()));
    }

    @Test
    void getConversionByTransactionId_returnsSingleDto() throws Exception {
        UUID transactionId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(123.45);

        ConversionResponseDTO dto = new ConversionResponseDTO(amount, transactionId);

        when(conversionService.getConversionByTransactionId(transactionId)).thenReturn(dto);

        mockMvc.perform(get("/api/conversions/by-id").param("transactionId", transactionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(amount.toString()))
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()));
    }
}