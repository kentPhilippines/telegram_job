package com.payment.bot.command.registry;

import com.payment.bot.command.base.Command;
import com.payment.bot.command.base.CommandContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, Command> callbackHandlers = new HashMap<>();

    public void registerCommand(Command command) {
        commands.put(command.getCommandName(), command);
    }

    public void executeCommand(String commandName, Update update) {
        Command command = commands.get(commandName);
        if (command != null) {
            CommandContext context = buildContext(update);
            command.execute(context);
        }
    }

    public void executeCallback(String callbackData, Update update) {
        String[] parts = callbackData.split("_");
        Command handler = callbackHandlers.get(parts[0]);
        if (handler != null) {
            CommandContext context = buildContext(update);
            handler.execute(context);
        }
    }

    private CommandContext buildContext(Update update) {
        return CommandContext.builder()
                .update(update)
                .user(getUser(update))
                .arguments(getArguments(update))
                .build();
    }

    private User getUser(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        }
        return null;
    }

    private String[] getArguments(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String[] parts = update.getMessage().getText().split(" ");
            if (parts.length > 1) {
                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);
                return args;
            }
        }
        return new String[0];
    }
} 