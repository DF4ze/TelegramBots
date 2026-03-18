# Telegram Spring Boot Module

A lightweight Telegram bot framework for Spring Boot applications.

This module provides:

- Telegram message sending (`TelegramSender`)
- Telegram long polling support
- Annotation-based message routing
- Command auto-registration in Telegram
- User whitelist security
- Automatic reply handling
- Command argument parsing

---

# Installation

Add the dependency:

```xml
<dependency>
    <groupId>com.yourorg</groupId>
    <artifactId>telegram-spring-module</artifactId>
    <version>0.0.1</version>
</dependency>
```

Configuration
telegram.bot-token=YOUR_BOT_TOKEN

telegram.polling-enabled=true

telegram.allowed-user-ids[0]=123456789
telegram.allowed-user-ids[1]=987654321

If the whitelist is empty, all users are allowed.

Sending messages
@Autowired
TelegramSender telegramSender;

telegramSender.sendMessage(chatId, "Hello!");
Creating a Telegram controller
@TelegramController
public class DemoTelegramController {

    @Command(value="/ping", description="Ping command")
    public String ping(TelegramUpdateContext ctx) {
        return "pong";
    }

    @Chat
    public void chat(TelegramUpdateContext ctx) {
        System.out.println(ctx.getText());
    }

}

# Commands

Commands are automatically registered in Telegram using the Bot API.

Example:

/ping - Ping command

They will appear in the Telegram command menu.

Command arguments

Example message:

/trade btc 100 market

Usage:

@Command("/trade")
public String trade(TelegramUpdateContext ctx) {

    String symbol = ctx.getArgs().get(0);
    String amount = ctx.getArgs().get(1);

    return "Trading " + symbol + " for " + amount;
}

Available fields:

ctx.getCommand()
ctx.getCommandArgsRaw()
ctx.getArgs()
Security (Whitelist)

If a user not in the whitelist sends /start, the bot replies with their Telegram ID so it can be added to configuration.

Handler rules

Only one @Chat handler allowed

@Command must follow Telegram format

Handler signature must be:

(TelegramUpdateContext ctx)

Invalid configuration fails at application startup.

Example
@TelegramController
public class TradingBot {

    @Command("/start")
    public String start(TelegramUpdateContext ctx) {
        return "Welcome to the bot!";
    }

    @Command("/price")
    public String price(TelegramUpdateContext ctx) {
        return "BTC price: 65000$";
    }

}
Design goals

This module follows the philosophy:

Spring MVC style for Telegram bots

Minimal configuration, annotation-based routing, and clear startup validation.