package com.zetta.conversion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application configuration class for defining Spring beans.
 * <p>
 * Provides the RestTemplate bean to be used for making RESTful API calls.
 */
@Configuration
public class AppConfig {

    /**
     * Creates and returns a new instance of {@link RestTemplate}.
     * <p>
     * This bean can be injected wherever REST calls are needed.
     *
     * @return a new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
