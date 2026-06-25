package com.suhani.aiquerygenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating RestTemplate bean.
 * RestTemplate is used for making HTTP requests to the Hugging Face Inference API.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean for HTTP communication.
     *
     * @return a new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}