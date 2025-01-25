package com.payment.bot.command.base;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Data
@Builder
public class CommandContext {
    private final Update update;
    private final User user;
    private final String[] arguments;
    private final boolean securityVerified;
} 