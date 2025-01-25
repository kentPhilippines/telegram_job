package com.payment.bot.bot;

import com.payment.bot.command.registry.CommandRegistry;
import com.payment.bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class PaymentBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CommandRegistry commandRegistry;

    public PaymentBot(BotConfig botConfig, 
                     CommandRegistry commandRegistry
                     ) {
        this.botConfig = botConfig;
        this.commandRegistry = commandRegistry;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            // 验证用户权限


            // 处理命令
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
            log.error("Error processing update: ", e);
        }
    }

    private void handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        if (text.startsWith("/")) {
            commandRegistry.executeCommand(text.split(" ")[0], update);
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        commandRegistry.executeCallback(callbackData, update);
    }
} 