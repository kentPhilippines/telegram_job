package com.payment.bot.command.base;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    void execute(CommandContext context);
    String getCommandName();
    String getDescription();
} 