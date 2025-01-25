# 支付系统辅助机器人命令设计

## 1. 核心命令体系

### 1.1 基础命令结构
```java
public interface PaymentCommand extends Command {
    String getCategory();        // 命令类别（订单、账户、统计等）
    boolean isSecurityRequired(); // 是否需要安全验证
    Permission getPermission();   // 权限等级
}
```

### 1.2 命令分类
1. 订单查询
2. 账户信息
3. 交易统计
4. 支付状态
5. 系统监控

## 2. 订单查询命令

### 2.1 订单状态查询
```java
@Component
public class OrderCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        String orderId = context.getArguments()[0];
        
        // 验证权限
        if (!hasOrderAccessPermission(context.getUser(), orderId)) {
            throw new PermissionDeniedException("无权查看该订单");
        }
        
        OrderInfo order = orderService.getOrder(orderId);
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatOrderInfo(order));
        message.setReplyMarkup(createOrderActionKeyboard(order));
    }
    
    private InlineKeyboardMarkup createOrderActionKeyboard(OrderInfo order) {
        return InlineKeyboardBuilder.create()
            .addRow(
                InlineButton.of("📋 详细信息", "order_detail_" + order.getId()),
                InlineButton.of("📊 支付流水", "order_flow_" + order.getId())
            )
            .addRow(
                InlineButton.of("❌ 退款查询", "refund_query_" + order.getId()),
                InlineButton.of("📱 联系商户", "contact_merchant_" + order.getMerchantId())
            )
            .build();
    }
}
```

### 2.2 批量订单查询
```java
@Component
public class OrderListCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        String timeRange = context.getArguments().length > 0 ? 
                          context.getArguments()[0] : "today";
                          
        OrderQueryFilter filter = createFilter(timeRange);
        List<OrderInfo> orders = orderService.queryOrders(filter);
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatOrderList(orders));
        message.setReplyMarkup(createOrderListKeyboard(timeRange));
    }
}
```

## 3. 账户信息命令

### 3.1 账户余额查询
```java
@Component
public class BalanceCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        // 需要二次验证
        if (!context.isSecurityVerified()) {
            startSecurityVerification(context);
            return;
        }
        
        AccountInfo account = accountService.getAccountInfo(
            context.getUser().getMerchantId()
        );
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatAccountInfo(account));
        message.setReplyMarkup(createAccountActionKeyboard());
    }
}
```

## 4. 交易统计命令

### 4.1 交易报表
```java
@Component
public class StatisticsCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        String timeRange = context.getArguments()[0]; // daily/weekly/monthly
        
        TransactionStats stats = statisticsService.getStats(
            context.getUser().getMerchantId(),
            timeRange
        );
        
        SendPhoto message = new SendPhoto();
        message.setChatId(context.getChatId());
        message.setPhoto(generateStatsChart(stats));
        message.setCaption(formatStatsReport(stats));
        message.setReplyMarkup(createStatsActionKeyboard(timeRange));
    }
}
```

## 5. 支付状态监控

### 5.1 支付通道状态
```java
@Component
public class ChannelStatusCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        List<ChannelStatus> channels = monitorService.getChannelStatus();
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatChannelStatus(channels));
        message.setReplyMarkup(createChannelDetailKeyboard());
    }
    
    private String formatChannelStatus(List<ChannelStatus> channels) {
        StringBuilder sb = new StringBuilder();
        sb.append("💳 支付通道状态\n\n");
        
        for (ChannelStatus channel : channels) {
            sb.append(String.format(
                "%s: %s\n" +
                "成功率: %.2f%%\n" +
                "平均耗时: %dms\n\n",
                channel.getName(),
                getStatusEmoji(channel.getStatus()),
                channel.getSuccessRate(),
                channel.getAverageTime()
            ));
        }
        
        return sb.toString();
    }
}
```

## 6. 快捷命令示例

```
/o 123456 - 查询订单号123456
/o today - 查询今日订单
/b - 查询账户余额
/s daily - 查看今日交易统计
/s weekly - 查看本周交易统计
/c - 查看支付通道状态
/alert - 设置异常提醒
```

## 7. 安全验证流程

```java
@Component
public class SecurityVerification {
    
    public void startVerification(CommandContext context) {
        // 生成验证码
        String code = generateVerificationCode();
        // 发送验证码到注册手机
        sendVerificationSms(context.getUser().getPhone(), code);
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("🔐 安全验证\n\n" +
                       "验证码已发送至手机 " + 
                       maskPhoneNumber(context.getUser().getPhone()) +
                       "\n请输入验证码：");
    }
    
    @Step(state = State.WAITING_CODE)
    public void handleVerificationCode(String code, ConversationContext context) {
        if (verifyCode(context.getUser().getPhone(), code)) {
            // 验证成功，继续执行原命令
            context.setSecurityVerified(true);
            resumeOriginalCommand(context);
        } else {
            throw new VerificationException("验证码错误，请重试");
        }
    }
}
```

## 8. 异常监控和提醒

```java
@Component
public class AlertCommand implements PaymentCommand {
    @Override
    public void execute(CommandContext context) {
        AlertSettings settings = alertService.getSettings(
            context.getUser().getMerchantId()
        );
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("⚠️ 异常提醒设置\n\n" +
                       formatAlertSettings(settings));
        
        message.setReplyMarkup(createAlertSettingsKeyboard());
    }
}
```

主要特点：
1. 专注于支付系统监控和查询
2. 严格的安全验证机制
3. 多维度的数据统计
4. 实时的通道监控
5. 异常预警系统
6. 完善的权限控制
7. 便捷的订单查询
8. 直观的数据展示

需要我详细说明某个具体功能吗？ 