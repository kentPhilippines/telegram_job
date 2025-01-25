# Telegram Bot 命令处理模块设计

## 1. 命令系统架构

### 1.1 命令处理流程
```ascii
User Input -> Command Parser -> Command Validator -> Command Handler -> Response Generator -> User Output
     ^                                                                                           |
     |                                                                                           |
     +------------------------------- State Management -------------------------------------------+
```

### 1.2 核心组件
```java
public interface Command {
    String getName();           // 命令名称
    String getDescription();    // 命令描述
    List<String> getAliases(); // 命令别名
    boolean requiresAuth();     // 是否需要认证
    void execute(CommandContext context);
}

public class CommandContext {
    private final Update update;
    private final User user;
    private final Chat chat;
    private final String[] arguments;
    private final UserSession session;
}

@Service
public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> aliasToCommand = new HashMap<>();
}
```

## 2. 基础命令设计

### 2.1 开始命令
```java
@Component
public class StartCommand implements Command {
    @Override
    public void execute(CommandContext context) {
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("👋 欢迎使用机器人！\n" +
                       "我可以帮助你完成以下任务：\n" +
                       "1. 设置提醒\n" +
                       "2. 查询信息\n" +
                       "3. 管理订阅\n\n" +
                       "使用 /help 查看详细指令说明");
        
        // 添加快速操作按钮
        message.setReplyMarkup(createWelcomeKeyboard());
    }
    
    private ReplyKeyboardMarkup createWelcomeKeyboard() {
        return KeyboardBuilder.create()
            .addRow("📝 创建提醒", "ℹ️ 帮助")
            .addRow("⚙️ 设置", "📊 统计")
            .build();
    }
}
```

### 2.2 帮助命令
```java
@Component
public class HelpCommand implements Command {
    @Override
    public void execute(CommandContext context) {
        String helpText = formatHelpText(context.getUser().getLanguage());
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(helpText);
        message.setParseMode(ParseMode.MARKDOWN);
        
        // 添加内联键盘，方便快速访问具体功能的帮助
        message.setReplyMarkup(createHelpInlineKeyboard());
    }
    
    private InlineKeyboardMarkup createHelpInlineKeyboard() {
        return InlineKeyboardBuilder.create()
            .addRow(
                InlineButton.of("提醒相关", "help_reminder"),
                InlineButton.of("查询相关", "help_query")
            )
            .addRow(
                InlineButton.of("设置相关", "help_settings"),
                InlineButton.of("常见问题", "help_faq")
            )
            .build();
    }
}
```

## 3. 交互式命令设计

### 3.1 设置提醒命令
```java
@Component
public class ReminderCommand implements Command {
    private final ConversationManager conversationManager;
    
    @Override
    public void execute(CommandContext context) {
        // 启动提醒设置会话
        Conversation conversation = conversationManager.start(
            context.getChatId(),
            ReminderFlow.class
        );
        
        // 发送第一步提示
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("📝 创建新提醒\n\n" +
                       "请输入提醒内容：");
        
        // 添加取消按钮
        message.setReplyMarkup(createCancelKeyboard());
    }
}

@Component
public class ReminderFlow implements ConversationFlow {
    private enum State {
        WAITING_CONTENT,
        WAITING_TIME,
        WAITING_REPEAT,
        CONFIRMATION
    }
    
    @Step(state = State.WAITING_CONTENT)
    public void handleContent(String content, ConversationContext context) {
        context.setData("content", content);
        
        // 请求时间
        SendMessage message = new SendMessage();
        message.setText("⏰ 请选择提醒时间：");
        message.setReplyMarkup(createTimePickerKeyboard());
    }
    
    @Step(state = State.WAITING_TIME)
    public void handleTime(String time, ConversationContext context) {
        // 处理时间输入...
    }
}
```

### 3.2 设置命令
```java
@Component
public class SettingsCommand implements Command {
    @Override
    public void execute(CommandContext context) {
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText("⚙️ 设置\n\n" +
                       "请选择要修改的设置项：");
        
        message.setReplyMarkup(createSettingsKeyboard());
    }
    
    private InlineKeyboardMarkup createSettingsKeyboard() {
        return InlineKeyboardBuilder.create()
            .addRow(
                InlineButton.of("🌐 语言设置", "settings_language"),
                InlineButton.of("⏰ 时区设置", "settings_timezone")
            )
            .addRow(
                InlineButton.of("🔔 通知设置", "settings_notifications"),
                InlineButton.of("👤 个人信息", "settings_profile")
            )
            .build();
    }
}
```

## 4. 用户体验优化

### 4.1 命令建议
```java
@Component
public class CommandSuggester {
    public List<BotCommand> suggestCommands(User user) {
        List<BotCommand> suggestions = new ArrayList<>();
        
        // 基于用户历史行为推荐命令
        UserStats stats = userStatsService.getStats(user.getId());
        suggestions.addAll(getMostUsedCommands(stats));
        
        // 基于当前时间推荐
        suggestions.addAll(getTimeBasedCommands(user.getTimezone()));
        
        return suggestions;
    }
}
```

### 4.2 智能帮助
```java
@Component
public class SmartHelpService {
    public String getContextualHelp(User user, String currentCommand) {
        // 获取用户当前上下文
        UserContext context = userContextService.getCurrentContext(user);
        
        // 生成针对性帮助内容
        return helpTemplateEngine.generate(
            currentCommand,
            context,
            user.getLanguage()
        );
    }
}
```

### 4.3 错误处理
```java
@Component
public class CommandErrorHandler {
    public void handleError(CommandExecutionException error, CommandContext context) {
        String errorMessage = switch (error.getType()) {
            case INVALID_FORMAT -> "❌ 输入格式不正确\n" +
                                 "正确格式：" + error.getExpectedFormat();
            case PERMISSION_DENIED -> "⛔️ 您没有执行此命令的权限";
            case RATE_LIMITED -> "⏳ 请稍后再试";
            default -> "🤖 抱歉，发生了一些错误";
        };
        
        SendMessage message = new SendMessage();
        message.setChatId(context.getChatId());
        message.setText(errorMessage);
        
        // 添加帮助按钮
        message.setReplyMarkup(createHelpKeyboard(error));
    }
}
```

## 5. 状态管理

### 5.1 用户会话
```java
@Component
public class UserSession {
    private final String userId;
    private final Map<String, Object> data;
    private Command currentCommand;
    private ConversationState conversationState;
    
    public void setCommandContext(Command command) {
        this.currentCommand = command;
        this.conversationState = ConversationState.STARTED;
    }
}
```

### 5.2 会话管理
```java
@Service
public class SessionManager {
    private final Cache<String, UserSession> sessionCache;
    
    public UserSession getOrCreateSession(String userId) {
        return sessionCache.get(userId, () -> {
            UserSession session = new UserSession(userId);
            initializeSession(session);
            return session;
        });
    }
}
```

## 6. 多语言支持

### 6.1 消息模板
```yaml
commands:
  start:
    zh_CN: |
      👋 欢迎使用机器人！
      我可以帮助你完成以下任务：
      1. 设置提醒
      2. 查询信息
      3. 管理订阅
    en_US: |
      👋 Welcome to the bot!
      I can help you with:
      1. Set reminders
      2. Query information
      3. Manage subscriptions
```

### 6.2 语言处理
```java
@Service
public class MessageService {
    public String getMessage(String key, String language, Object... args) {
        String template = messageSource.getMessage(
            key,
            null,
            new Locale(language)
        );
        return MessageFormat.format(template, args);
    }
}
```

这个命令处理模块设计的特点：

1. 清晰的命令处理流程
2. 丰富的用户交互界面
3. 支持多步骤对话
4. 智能的命令建议
5. 完善的错误处理
6. 多语言支持
7. 状态管理机制

需要对某个具体部分做更详细的说明吗？