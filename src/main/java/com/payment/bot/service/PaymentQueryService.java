package com.payment.bot.service;

import com.payment.bot.config.PaymentApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService {
    private final PaymentApiConfig config;
    private final RestTemplate restTemplate;
    
    public String queryOrderStatus(String apiKey, String orderId) {
        String url = config.getBaseUrl() + "/api/order/status";
        return executeQuery(url + "?orderId=" + orderId, apiKey);
    }
    
    public String queryChannelStatus(String apiKey) {
        String url = config.getBaseUrl() + "/api/channel/status";
        return executeQuery(url, apiKey);
    }
    
    public String queryDailySummary(String apiKey) {
        String url = config.getBaseUrl() + "/api/summary/daily";
        return executeQuery(url, apiKey);
    }
    
    private String executeQuery(String url, String apiKey) {
        try {
            return restTemplate.getForObject(
                url + (url.contains("?") ? "&" : "?") + "apiKey=" + apiKey, 
                String.class
            );
        } catch (Exception e) {
            log.error("Query failed: {}", e.getMessage());
            throw new RuntimeException("查询失败，请稍后重试");
        }
    }
} 