package com.payment.bot.repository;

import com.payment.bot.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByApiKey(String apiKey);
} 