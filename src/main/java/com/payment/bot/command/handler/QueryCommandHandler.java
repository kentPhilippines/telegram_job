package com.payment.bot.command.handler;

import com.payment.bot.command.base.Command;
import com.payment.bot.command.base.CommandContext;
import com.payment.bot.service.PaymentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
public class QueryCommandHandler implements Command {
    private final PaymentQueryService queryService;
    
    public QueryCommandHandler(PaymentQueryService queryService) {
        this.queryService = queryService;
    }
    
    @Override
    public void execute(CommandContext context) {
        String[] args = context.getArguments();
        if (args.length < 2) {
            sendHelpMessage(context);
            return;
        }
        
        String queryType = args[0];
        String queryValue = args[1];
        String merchantId = "MERCHANT_ID"; // 从context或配置中获取
        
        String result;
        switch (queryType) {
            case "order":
                result = queryService.queryOrderStatus(merchantId, queryValue);
                break;
            case "channel":
                result = queryService.queryChannelStatus(merchantId);
                break;
            case "summary":
                result = queryService.queryDailySummary(merchantId);
                break;
            default:
                result = "未知的查询类型";
        }
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getUpdate().getMessage().getChatId().toString());
        message.setText(formatQueryResult(result));
    }
    
    @Override
    public String getCommandName() {
        return "/query";
    }
    
    @Override
    public String getDescription() {
        return "查询支付信息，用法：\n" +
               "/query order ORDER_ID - 查询订单状态\n" +
               "/query channel - 查询通道状态\n" +
               "/query summary - 查询日交易汇总";
    }
    
    private void sendHelpMessage(CommandContext context) {
        SendMessage message = new SendMessage();
        message.setChatId(context.getUpdate().getMessage().getChatId().toString());
        message.setText(getDescription());
    }
    
    private String formatQueryResult(String result) {
        // 格式化查询结果为易读的形式
        return result; // 实际使用时需要更好的格式化
    }
} 