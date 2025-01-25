package com.payment.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {
    private String username;
    private String token;
    private Webhook webhook = new Webhook();
    
    @Data
    public static class Webhook {
        private String url;
    }
} 