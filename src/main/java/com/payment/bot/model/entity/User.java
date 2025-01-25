package com.payment.bot.model.entity;

import com.payment.bot.model.enums.UserRole;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long telegramId;
    
    private String username;
    
    @Column(nullable = false)
    private String apiKey;  // 用于访问第三方支付系统的API密钥
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private boolean enabled = true;
    
    private LocalDateTime lastLoginTime;
    
    private LocalDateTime createTime;
} 