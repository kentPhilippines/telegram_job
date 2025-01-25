# 金融交易机器人命令设计

## 1. 核心命令体系

### 1.1 基础命令结构
```java
public interface FinancialCommand extends Command {
    String getCategory();        // 命令类别（市场、交易、分析等）
    boolean isRealTime();        // 是否需要实时数据
    Permission getPermission();  // 权限等级
}
```

### 1.2 命令分类
1. 市场信息查询
2. 技术分析
3. 价格提醒
4. 个人持仓
5. 新闻资讯

## 2. 市场信息命令

### 2.1 价格查询
```java
@Component
public class PriceCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        String symbol = context.getArguments()[0].toUpperCase();
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatPriceInfo(symbol));
        message.setReplyMarkup(createPriceActionKeyboard(symbol));
    }
    
    private InlineKeyboardMarkup createPriceActionKeyboard(String symbol) {
        return InlineKeyboardBuilder.create()
            .addRow(
                InlineButton.of("📊 K线图", "chart_" + symbol),
                InlineButton.of("📈 技术分析", "analysis_" + symbol)
            )
            .addRow(
                InlineButton.of("⚡ 设置提醒", "alert_" + symbol),
                InlineButton.of("📰 相关新闻", "news_" + symbol)
            )
            .build();
    }
}
```

### 2.2 市场概览
```java
@Component
public class MarketCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        MarketOverview overview = marketService.getOverview();
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatMarketOverview(overview));
        message.setReplyMarkup(createMarketDetailKeyboard());
    }
}
```

## 3. 技术分析命令

### 3.1 指标分析
```java
@Component
public class IndicatorCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        String symbol = context.getArguments()[0];
        String indicator = context.getArguments()[1];
        
        TechnicalAnalysis analysis = analysisService.getIndicator(
            symbol, 
            indicator
        );
        
        SendPhoto message = new SendPhoto();
        message.setChatId(context.getChatId());
        message.setPhoto(generateIndicatorChart(analysis));
        message.setCaption(formatAnalysisResult(analysis));
        message.setReplyMarkup(createTimeframeKeyboard(symbol, indicator));
    }
}
```

### 3.2 信号提示
```java
@Component
public class SignalCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        String symbol = context.getArguments()[0];
        
        List<TradingSignal> signals = signalService.getSignals(symbol);
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatSignals(signals));
        message.setReplyMarkup(createSignalActionKeyboard(symbol));
    }
}
```

## 4. 价格提醒命令

### 4.1 设置提醒
```java
@Component
public class AlertCommand implements FinancialCommand {
    
    @Step(state = State.INIT)
    public void startAlert(CommandContext context) {
        String symbol = context.getArguments()[0];
        
        SendMessage message = new SendMessage();
        message.setText("📊 " + symbol + " 当前价格: " + 
                       getPriceInfo(symbol) + "\n\n" +
                       "请选择提醒类型：");
        
        message.setReplyMarkup(createAlertTypeKeyboard());
    }
    
    @Step(state = State.WAITING_PRICE)
    public void handlePrice(String price, ConversationContext context) {
        // 验证价格输入
        if (!isPriceValid(price)) {
            throw new InvalidInputException("请输入有效的价格");
        }
        
        // 设置提醒
        String symbol = context.getData("symbol");
        AlertType type = context.getData("alertType");
        priceAlertService.setAlert(symbol, price, type);
        
        // 发送确认
        SendMessage message = new SendMessage();
        message.setText("✅ 提醒设置成功！\n" +
                       "当 " + symbol + " " + 
                       formatAlertCondition(type, price) + 
                       " 时，您将收到通知。");
    }
}
```

## 5. 个人持仓命令

### 5.1 持仓查询
```java
@Component
public class PortfolioCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        Portfolio portfolio = portfolioService.getPortfolio(
            context.getUser().getId()
        );
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatPortfolio(portfolio));
        message.setReplyMarkup(createPortfolioActionKeyboard());
    }
    
    private String formatPortfolio(Portfolio portfolio) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 当前持仓\n\n");
        
        for (Position position : portfolio.getPositions()) {
            sb.append(String.format(
                "%s: %.2f @ %.2f\n" +
                "盈亏: %.2f%%\n",
                position.getSymbol(),
                position.getQuantity(),
                position.getEntryPrice(),
                position.getPnlPercentage()
            ));
        }
        
        return sb.toString();
    }
}
```

## 6. 新闻资讯命令

### 6.1 新闻查询
```java
@Component
public class NewsCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        String symbol = context.getArguments().length > 0 ? 
                       context.getArguments()[0] : null;
        
        List<NewsItem> news = newsService.getNews(symbol);
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(formatNews(news));
        message.setReplyMarkup(createNewsFilterKeyboard());
    }
}
```

## 7. 用户偏好设置

### 7.1 交易偏好
```java
@Component
public class PreferenceCommand implements FinancialCommand {
    @Override
    public void execute(CommandContext context) {
        UserPreferences prefs = preferencesService.getPreferences(
            context.getUser().getId()
        );
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("⚙️ 交易偏好设置\n\n" +
                       formatPreferences(prefs));
        
        message.setReplyMarkup(createPreferencesKeyboard());
    }
}
```

## 8. 快捷命令示例

```
/p btc - 查看比特币价格
/p eth - 查看以太坊价格
/m - 市场概览
/alert btc>50000 - 设置比特币价格提醒
/news btc - 查看比特币相关新闻
/ma btc 200 - 查看比特币200日均线
/rsi btc - 查看比特币RSI指标
/pos - 查看当前持仓
```

## 9. 错误处理

```java
@Component
public class FinancialCommandErrorHandler extends CommandErrorHandler {
    @Override
    public void handleError(CommandExecutionException error, CommandContext context) {
        String errorMessage = switch (error.getType()) {
            case SYMBOL_NOT_FOUND -> "❌ 未找到该交易对\n" +
                                   "请检查输入的交易对是否正确";
            case RATE_LIMIT -> "⏳ 请求过于频繁，请稍后再试";
            case DATA_ERROR -> "📊 数据获取失败，请稍后重试";
            case PERMISSION_DENIED -> "🔒 您需要升级到高级账户才能使用该功能";
            default -> "🤖 操作失败，请稍后重试";
        };
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(errorMessage);
        message.setReplyMarkup(createErrorHelpKeyboard(error));
    }
}
```

主要特点：
1. 专注于金融数据查询和分析
2. 实时市场数据支持
3. 技术分析图表生成
4. 价格提醒功能
5. 个人持仓管理
6. 新闻资讯整合
7. 多样的交互方式
8. 专业的错误处理

需要我详细说明某个具体功能吗？ 