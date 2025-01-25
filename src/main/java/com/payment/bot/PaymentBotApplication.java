package com.payment.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentBotApplication.class, args);
    }
} 