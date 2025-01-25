# Telegram Bot 架构设计文档

## 1. 系统架构

### 1.1 整体架构 

```ascii
                                    +-----------------+
                                    |                 |
                                    | Telegram Server |
                                    |                 |
                                    +--------+--------+
                                             |
                                             | HTTPS
                                             |
                    +------------------------+------------------------+
                    |                        |                       |
                    |                        |                       |
              +-----+-----+            +-----+-----+           +-----+-----+
              |   Web    |            |   Bot     |           |  Message  |
              | Server   |            |  Core     |           | Handler   |
              +---------++            +-----------+           +-----------+
                        |                   |                       |
                        |                   |                       |
                 +------+-------------------+-------------------+---+
                 |                                             |
           +-----+-----+                               +-------+-------+
           | Database  |                               |  Redis Cache  |
           +-----------+                               +---------------+
```

### 1.2 技术栈选型

- 核心框架：Spring Boot 2.7.x
- 构建工具：Maven
- 数据库：PostgreSQL 14
- 缓存：Redis 6.x
- 消息队列：RabbitMQ
- 监控：Spring Boot Admin + Prometheus + Grafana
- 日志：ELK Stack (Elasticsearch + Logstash + Kibana)

### 1.3 分层架构

```ascii
+------------------------+
|     Presentation      |
|  - WebhookController  |
|  - BotController      |
+------------------------+
|      Application      |
|   - BotFacade        |
|   - CommandService    |
|   - MessageService    |
+------------------------+
|       Domain          |
|   - Entity           |
|   - Repository       |
|   - Domain Service   |
+------------------------+
|    Infrastructure     |
|   - Database         |
|   - Cache            |
|   - External API     |
+------------------------+
```

## 2. 核心模块设计

### 2.1 Bot 核心模块

```java
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final UpdateProcessor updateProcessor;
    private final CommandRegistry commandRegistry;
    private final MessageRouter messageRouter;
    
    @Override
    public void onUpdateReceived(Update update) {
        try {
            updateProcessor.process(update);
        } catch (Exception e) {
            log.error("Error processing update", e);
            errorHandler.handle(e);
        }
    }
}
```

### 2.2 命令处理模块

```java
public interface BotCommand {
    void execute(Update update);
    String getCommandName();
    String getDescription();
}

@Component
public class CommandRegistry {
    private final Map<String, BotCommand> commands = new HashMap<>();
    
    @PostConstruct
    public void init() {
        register("/start", new StartCommand());
        register("/help", new HelpCommand());
        register("/settings", new SettingsCommand());
    }
}
```

### 2.3 消息处理模块

```java
public interface MessageHandler {
    boolean canHandle(Update update);
    void handle(Update update);
}

@Component
public class TextMessageHandler implements MessageHandler {
    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
    
    @Override
    public void handle(Update update) {
        // 处理文本消息
    }
}
```

## 3. 数据模型设计

### 3.1 用户模型

```java
@Entity
@Table(name = "bot_users")
public class User {
    @Id
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private String languageCode;
    private UserState state;
    private LocalDateTime lastInteractionTime;
    
    @OneToMany(mappedBy = "user")
    private List<Message> messages;
}
```

### 3.2 消息模型

```java
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Long chatId;
    private String content;
    private MessageType type;
    private LocalDateTime createTime;
    
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
```

## 4. 接口设计

### 4.1 Webhook 接口

```java
@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    
    @PostMapping("/{token}")
    public ResponseEntity<Void> handleUpdate(
            @PathVariable String token,
            @RequestBody Update update) {
        botService.processUpdate(update);
        return ResponseEntity.ok().build();
    }
}
```

### 4.2 Bot API 服务

```java
@Service
@Slf4j
public class BotApiService {
    private final TelegramBot bot;
    
    public void sendMessage(SendMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
            throw new BotApiException("Message sending failed", e);
        }
    }
}
```

## 5. 缓存设计

### 5.1 Redis 缓存配置

```java
@Configuration
@EnableCaching
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

### 5.2 缓存策略

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#telegramId")
    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
            .orElseThrow(() -> new UserNotFoundException(telegramId));
    }
    
    @CacheEvict(value = "users", key = "#user.telegramId")
    public void updateUser(User user) {
        userRepository.save(user);
    }
}
```

## 6. 安全设计

### 6.1 安全配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/webhook/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .build();
    }
}
```

### 6.2 Token 验证

```java
@Component
public class TokenValidator {
    private final String botToken;
    
    public boolean validateToken(String token) {
        return StringUtils.isNotBlank(token) && 
               token.equals(botToken);
    }
}
```

## 7. 监控设计

### 7.1 性能监控

```java
@Aspect
@Component
public class PerformanceMonitor {
    
    @Around("@annotation(Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) 
            throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 记录性能指标到 Prometheus
        return result;
    }
}
```

### 7.2 健康检查

```java
@Component
public class BotHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 检查 Bot API 连接状态
            return Health.up().build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

## 8. 部署架构

### 8.1 生产环境部署架构

```ascii
+------------------------+      +------------------------+
|    Nginx (负载均衡)     |      |    应用服务器集群        |
|                        +----->|    - Bot Instance 1   |
|    - SSL 终止          |      |    - Bot Instance 2   |
|    - 请求转发           |      |    - Bot Instance 3   |
+------------------------+      +------------------------+
           |                              |
           |                              |
+------------------------+      +------------------------+
|    Redis 集群           |      |    PostgreSQL 主从     |
|    - 会话缓存           |      |    - 主库              |
|    - 数据缓存           |      |    - 从库              |
+------------------------+      +------------------------+
```

### 8.2 容器化部署

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## 9. 配置管理

### 9.1 应用配置

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
    
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    
telegram:
  bot:
    token: ${BOT_TOKEN}
    username: ${BOT_USERNAME}
    webhook:
      url: ${WEBHOOK_URL}
      
logging:
  level:
    root: INFO
    com.example.telegrambot: DEBUG
```

### 9.2 环境变量

```properties
# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# 数据库配置
DB_HOST=postgres
DB_PORT=5432
DB_NAME=telegram_bot
DB_USERNAME=bot_user
DB_PASSWORD=secret

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=secret

# Bot配置
BOT_TOKEN=your_bot_token
BOT_USERNAME=your_bot_username
WEBHOOK_URL=https://your-domain.com/webhook
```

## 10. 错误处理

### 10.1 全局异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BotApiException.class)
    public ResponseEntity<ErrorResponse> handleBotApiException(
            BotApiException ex) {
        ErrorResponse error = new ErrorResponse(
            "BOT_API_ERROR",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}
```

### 10.2 业务异常定义

```java
public class BotApiException extends RuntimeException {
    public BotApiException(String message) {
        super(message);
    }
    
    public BotApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 11. 测试策略

### 11.1 单元测试

```java
@SpringBootTest
class CommandHandlerTest {
    
    @MockBean
    private BotApiService botApiService;
    
    @Autowired
    private CommandHandler commandHandler;
    
    @Test
    void testStartCommand() {
        Update update = createMockUpdate("/start");
        commandHandler.handle(update);
        
        verify(botApiService).sendMessage(any(SendMessage.class));
    }
}
```

### 11.2 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testWebhook() throws Exception {
        mockMvc.perform(post("/api/webhook/{token}", "valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createMockUpdateJson()))
            .andExpect(status().isOk());
    }
}
```

这个架构文档详细描述了：
1. 系统整体架构和技术栈
2. 核心模块的具体实现
3. 数据模型设计
4. API接口设计
5. 缓存策略
6. 安全机制
7. 监控系统
8. 部署架构
9. 配置管理
10. 错误处理
11. 测试策略

需要我对某个具体部分做更详细的说明吗？ 