package com.payment.bot.model.dto;

import lombok.Data;

@Data
public class PaymentQuery {
    private String merchantId;      // 商户ID
    private String orderId;         // 订单号
    private QueryType queryType;    // 查询类型
    
    public enum QueryType {
        ORDER_STATUS,      // 订单状态查询
        PAYMENT_CHANNEL,   // 支付通道查询
        DAILY_SUMMARY     // 日交易汇总
    }
} 