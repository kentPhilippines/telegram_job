telegram-payment-bot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── payment/
│   │   │           └── bot/
│   │   │               ├── PaymentBotApplication.java
│   │   │               ├── config/
│   │   │               │   ├── BotConfig.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── bot/
│   │   │               │   ├── PaymentBot.java
│   │   │               │   └── BotInitializer.java
│   │   │               ├── command/
│   │   │               │   ├── base/
│   │   │               │   │   ├── Command.java
│   │   │               │   │   ├── PaymentCommand.java
│   │   │               │   │   └── CommandContext.java
│   │   │               │   ├── handler/
│   │   │               │   │   ├── OrderCommandHandler.java
│   │   │               │   │   ├── BalanceCommandHandler.java
│   │   │               │   │   └── StatisticsCommandHandler.java
│   │   │               │   └── registry/
│   │   │               │       └── CommandRegistry.java
│   │   │               ├── service/
│   │   │               │   ├── OrderService.java
│   │   │               │   ├── AccountService.java
│   │   │               │   ├── StatisticsService.java
│   │   │               │   ├── MonitorService.java
│   │   │               │   └── SecurityService.java
│   │   │               ├── model/
│   │   │               │   ├── entity/
│   │   │               │   │   ├── User.java
│   │   │               │   │   ├── Order.java
│   │   │               │   │   └── Channel.java
│   │   │               │   └── dto/
│   │   │               │       ├── OrderInfo.java
│   │   │               │       ├── AccountInfo.java
│   │   │               │       └── ChannelStatus.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── OrderRepository.java
│   │   │               │   └── ChannelRepository.java
│   │   │               └── util/
│   │   │                   ├── MessageFormatter.java
│   │   │                   └── SecurityUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── com/
│               └── payment/
│                   └── bot/
│                       └── command/
│                           └── handler/
│                               └── OrderCommandHandlerTest.java
├── pom.xml
├── Dockerfile
└── README.md 