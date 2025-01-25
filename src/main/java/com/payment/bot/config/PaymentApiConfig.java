package com.payment.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment.api")
public class PaymentApiConfig {
    private String baseUrl;
    private int timeout = 5000;
} 