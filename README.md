# TelegramBots

TelegramBots est un module Spring Boot qui simplifie la création de bots Telegram en Java avec une approche inspirée de Spring MVC.
Il permet de déclarer des contrôleurs, router des commandes via annotations et gérer plusieurs bots avec une configuration centralisée.
L’objectif : rendre le développement de bots Telegram clair, structuré et rapide à mettre en place.

## Sommaire

- [Vue d'ensemble](#vue-densemble)
- [Fonctionnalités](#fonctionnalités)
- [Pré-requis](#pré-requis)
- [Installation](#installation)
- [Configuration](#configuration)
    - [Activation globale](#activation-globale)
    - [Configuration multi-bot](#configuration-multi-bot)
    - [Sécurité](#sécurité)
    - [Retry](#retry)
- [Démarrage rapide](#démarrage-rapide)
- [Annotations disponibles](#annotations-disponibles)
- [Contexte d'update](#contexte-dupdate)
- [Types de retour des handlers](#types-de-retour-des-handlers)
- [Gestion des commandes](#gestion-des-commandes)
- [Gestion des callback queries](#gestion-des-callback-queries)
- [Vues Telegram et boutons inline](#vues-telegram-et-boutons-inline)
- [API d'envoi](#api-denvoi)
- [Enregistrement automatique des commandes](#enregistrement-automatique-des-commandes)
- [Architecture interne](#architecture-interne)
- [Règles de validation au démarrage](#règles-de-validation-au-démarrage)
- [Limites et points à vérifier](#limites-et-points-à-vérifier)

## Vue d'ensemble

`TelegramBots` est un module Spring Boot qui fournit :

- une intégration **long polling** Telegram ;
- un système de routing basé sur des **annotations** ;
- une API d'envoi centralisée via `TelegramSender` ;
- une configuration **multi-bot** ;
- une **whitelist** par bot ;
- l'auto-enregistrement des commandes Telegram ;
- une prise en charge des **callback queries** ;
- un objet de contexte unique pour les handlers ;
- un support simple des vues avec **boutons inline**.

L'objectif est de permettre d'écrire des bots Telegram en Java avec une approche déclarative, lisible et cohérente avec l'écosystème Spring.

## Fonctionnalités

### Déjà présentes dans le code

- Support **multi-bot** via `telegram.bots`.
- Routing des messages texte avec `@Command` et `@Chat`.
- Routing des callbacks via `@CallbackQuery`.
- Construction d'un `TelegramUpdateContext` unifié.
- Envoi de :
    - messages texte ;
    - messages Markdown ;
    - photos ;
    - documents ;
    - vues avec clavier inline.
- **Auto-registration** des commandes via l'API Telegram.
- Configuration du **menu button** Telegram pour afficher les commandes.
- **Retry** configurable pour les appels d'envoi.
- **Whitelist** d'utilisateurs autorisés par bot.
- Validation de configuration au démarrage.

### Philosophie

- peu de configuration côté code applicatif ;
- handlers lisibles ;
- validation précoce des erreurs ;
- centralisation des comportements Telegram récurrents.


## Pré-requis

- **Java 21**
- **Spring Boot 4.0.3**
- dépendances Telegram basées sur :
    - `telegrambots-longpolling`
    - `telegrambots-client`

## Installation


```xml
<groupId>fr.ses10doigts</groupId>
<artifactId>TelegramBots</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

Si le module est consommé localement ou via un dépôt Maven privé, la dépendance ressemblera à ceci :

```xml
<dependency>
    <groupId>fr.ses10doigts</groupId>
    <artifactId>TelegramBots</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

### Activation globale

Le module s'active avec :

```yaml
telegram:
  enabled: true
```

Si `telegram.enabled=false`, l'auto-configuration spécifique Telegram n'est pas activée.

### Configuration multi-bot

La structure de configuration actuelle est une **liste de bots** :

```yaml
telegram:
  enabled: true
  bots:
    - id: main-bot
      token: ${TELEGRAM_MAIN_BOT_TOKEN}
      polling-enabled: true
      auto-register-commands: true
      configure-menu-button: true
      security:
        allowed-user-ids:
          - 123456789
          - 987654321
      retry:
        enabled: true
        max-attempts: 3
        delay-seconds: 2

    - id: admin-bot
      token: ${TELEGRAM_ADMIN_BOT_TOKEN}
      polling-enabled: true
      auto-register-commands: false
      configure-menu-button: false
      security:
        allowed-user-ids: []
      retry:
        enabled: false
```

### Propriétés d'un bot

| Propriété | Description | Valeur par défaut |
|---|---|---|
| `id` | identifiant interne du bot | aucune |
| `token` | token Telegram | aucune |
| `polling-enabled` | active le polling pour ce bot | `true` |
| `auto-register-commands` | enregistre les commandes au démarrage | `true` |
| `configure-menu-button` | configure le menu Telegram sur la liste des commandes | `true` |
| `security.allowed-user-ids` | whitelist d'utilisateurs | vide |
| `retry.enabled` | active le retry sur les envois | `false` |
| `retry.max-attempts` | nombre max d'essais | `1` |
| `retry.delay-seconds` | délai entre essais | `1` |

## Sécurité

Chaque bot possède sa propre configuration de sécurité :

```yaml
telegram:
  bots:
    - id: main-bot
      token: ${TOKEN}
      security:
        allowed-user-ids:
          - 123456789
```

### Comportement

- si la whitelist est vide, tous les utilisateurs sont autorisés ;
- si la whitelist contient des IDs, seuls ces utilisateurs sont acceptés ;
- si un utilisateur non autorisé envoie `/start`, le bot renvoie son identifiant Telegram pour faciliter l'ajout à la configuration.

## Retry

Le `DefaultTelegramSender` supporte un mécanisme de retry simple :

```yaml
telegram:
  bots:
    - id: main-bot
      token: ${TOKEN}
      retry:
        enabled: true
        max-attempts: 3
        delay-seconds: 2
```

Le retry :

- s'applique aux appels d'envoi Telegram ;
- n'est pas déclenché pour certains cas considérés comme non retryables, notamment certaines erreurs `400`, `403` et `404`.

## Démarrage rapide

### Contrôleur minimal

```java
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class DemoTelegramController {

    @Command(value = "/ping", description = "Ping command")
    public String ping(TelegramUpdateContext ctx) {
        return "pong";
    }

    @Chat
    public void chat(TelegramUpdateContext ctx) {
        System.out.println(ctx.getText());
    }
}
```

### Service applicatif qui envoie un message

```java
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramSender telegramSender;

    public void notify(Long chatId, String message) {
        telegramSender.sendMessage(chatId, message);
    }
}
```

## Annotations disponibles

### `@TelegramController`

Marque une classe comme contrôleur Telegram détecté par le module.

```java
@TelegramController
public class MyTelegramController {
}
```

### `@Command`

Associe une méthode à une commande Telegram.

```java
@Command(value = "/price", description = "Show current price")
public String price(TelegramUpdateContext ctx) {
    return "BTC price: 65000$";
}
```

### `@Chat`

Associe une méthode à la réception d'un message texte non routé par commande.

```java
@Chat
public void onChat(TelegramUpdateContext ctx) {
    System.out.println(ctx.getText());
}
```

### `@CallbackQuery`

Associe une méthode à une valeur de callback précise.

```java
@CallbackQuery("menu:open")
public String onMenuOpen(TelegramUpdateContext ctx) {
    return "Menu opened";
}
```

## Contexte d'update

Le module construit un `TelegramUpdateContext` à partir de l'`Update` Telegram.

### Champs principaux

- `botId`
- `update`
- `message`
- `chatId`
- `userId`
- `text`
- `command`
- `commandArgsRaw`
- `args`
- `callbackQuery`
- `callbackData`

### Exemple

```java
@Command("/trade")
public String trade(TelegramUpdateContext ctx) {
    String command = ctx.getCommand();
    String rawArgs = ctx.getCommandArgsRaw();
    String firstArg = ctx.getArgs().isEmpty() ? null : ctx.getArgs().get(0);

    return "command=" + command + ", raw=" + rawArgs + ", firstArg=" + firstArg;
}
```

## Types de retour des handlers

Les handlers peuvent actuellement retourner :

### `void`

Aucune réponse automatique n'est envoyée.

```java
@Chat
public void chat(TelegramUpdateContext ctx) {
    System.out.println(ctx.getText());
}
```

### `String`

La chaîne retournée est envoyée automatiquement comme message texte.

```java
@Command("/ping")
public String ping(TelegramUpdateContext ctx) {
    return "pong";
}
```

### `TelegramView`

Permet d'envoyer un message enrichi avec boutons inline.

```java
@Command("/menu")
public TelegramView menu(TelegramUpdateContext ctx) {
    return TelegramView.builder()
            .text("Choisis une action")
            .buttons(List.of(
                    List.of(
                            new TelegramButtonView("Open", "menu:open"),
                            new TelegramButtonView("Close", "menu:close")
                    )
            ))
            .build();
}
```

## Gestion des commandes

Le parsing des commandes est pris en charge par `ParsedCommand`.

### Comportements observés

- une commande doit commencer par `/` ;
- les arguments sont découpés par espaces ;
- la notation Telegram `/command@BotName` est normalisée vers `/command` ;
- `commandArgsRaw` contient la partie brute après la commande ;
- `args` contient la liste des arguments découpés.

### Exemple

Message reçu :

```text
/trade btc 100 market
```

Résultat exploitable :

- `ctx.getCommand()` → `/trade`
- `ctx.getCommandArgsRaw()` → `btc 100 market`
- `ctx.getArgs()` → `["btc", "100", "market"]`

## Gestion des callback queries

Les callbacks sont gérés via `@CallbackQuery`.

### Exemple complet

```java
@Command("/menu")
public TelegramView menu(TelegramUpdateContext ctx) {
    return TelegramView.builder()
            .text("Menu")
            .buttons(List.of(
                    List.of(
                            new TelegramButtonView("Ping", "menu:ping")
                    )
            ))
            .build();
}

@CallbackQuery("menu:ping")
public String onPing(TelegramUpdateContext ctx) {
    return "pong";
}
```

### Comportement interne

Lorsqu'un callback est reçu :

1. le module envoie d'abord un `answerCallbackQuery` pour arrêter le spinner côté Telegram ;
2. il reconstruit un `TelegramUpdateContext` ;
3. il cherche un handler correspondant à `callbackData` ;
4. il invoque le handler trouvé.

## Vues Telegram et boutons inline

Le module fournit deux modèles :

### `TelegramButtonView`

```java
new TelegramButtonView("Open", "menu:open");
```

### `TelegramView`

```java
TelegramView.builder()
        .text("Question :")
        .buttons(List.of(
                List.of(
                        new TelegramButtonView("Yes", "answer:yes"),
                        new TelegramButtonView("No", "answer:no")
                )
        ))
        .build();
```

### Structure des boutons

Le champ `buttons` est une liste de lignes :

```java
List<List<TelegramButtonView>>
```

Chaque sous-liste représente une ligne du clavier inline.

### Règles observées

- si `TelegramView` est `null`, rien n'est envoyé ;
- si le texte est vide et qu'il n'y a aucun bouton, rien n'est envoyé ;
- si des boutons invalides sont fournis, ils sont ignorés ;
- si des boutons existent mais que le texte est vide, le sender utilise un texte par défaut (`"Question :"`).

## API d'envoi

L'interface `TelegramSender` expose actuellement :

```java
public interface TelegramSender {
    void sendMessage(Long chatId, String text);
    void sendMarkdownMessage(Long chatId, String text);
    void sendMarkdownMessagePreservingLinks(Long chatId, String text);
    void sendPhoto(Long chatId, String photoPath, String caption);
    void sendDocument(Long chatId, String documentPath, String caption);
    void sendView(Long chatId, TelegramView view);
    void answerCallbackQuery(String callbackQueryId);
}
```

### Exemples

#### Message simple

```java
telegramSender.sendMessage(chatId, "Hello!");
```

#### Message Markdown

```java
telegramSender.sendMarkdownMessage(chatId, "*bold*");
```

#### Message Markdown en préservant les liens

```java
telegramSender.sendMarkdownMessagePreservingLinks(
        chatId,
        "Consulte https://example.com pour plus d'informations"
);
```

#### Envoi de photo

```java
telegramSender.sendPhoto(chatId, "/tmp/chart.png", "Daily chart");
```

#### Envoi de document

```java
telegramSender.sendDocument(chatId, "/tmp/report.pdf", "Weekly report");
```

## Enregistrement automatique des commandes

Au démarrage, `TelegramCommandRegistrar` :

- collecte les commandes déclarées ;
- les convertit en `BotCommand` ;
- appelle l'API Telegram `setMyCommands` ;
- peut aussi configurer le bouton de menu vers `MenuButtonCommands`.

### Description de commande

Si `description` est vide, la commande elle-même est utilisée comme description de secours.

## Architecture interne

### 1. `TelegramAutoConfiguration`

Déclare les beans principaux :

- `TelegramStartupValidator`
- `CurrentTelegramBotContext`
- `TelegramBotRegistry`
- `TelegramSenderRegistry`
- `TelegramSender`
- `TelegramHandlerRegistry`
- `TelegramUpdateDispatcher`
- les adapters de polling
- `TelegramCommandRegistrar`

### 2. `TelegramBotRegistry`

Centralise les bots configurés et permet de les retrouver par `id`.

### 3. `TelegramSenderRegistry`

Construit un `DefaultTelegramSender` par bot.

### 4. `ContextAwareTelegramSender`

Résout automatiquement le bon sender en fonction du bot courant lié au thread.

### 5. `TelegramPollingBotAdapter`

Associe un `botId` à chaque update traité, puis délègue au dispatcher.

### 6. `TelegramUpdateDispatcher`

Rôle principal :

- distinguer messages et callbacks ;
- construire le contexte ;
- appliquer la sécurité ;
- résoudre le bon handler ;
- exécuter le handler ;
- envoyer automatiquement la réponse si nécessaire.

### 7. `TelegramHandlerRegistry`

Scanne les beans annotés `@TelegramController` et enregistre :

- les handlers de commandes ;
- les handlers de chat ;
- les handlers de callback ;
- les définitions de commandes.

## Règles de validation au démarrage

Le code actuel valide plusieurs points dès l'initialisation :

### Configuration des bots

- `telegram.enabled=true` nécessite au moins un bot configuré ;
- chaque bot doit avoir un `id` non vide ;
- chaque bot doit avoir un `token` non vide ;
- les `id` doivent être uniques.

### Handlers

- la signature attendue est :
  ```java
  method(TelegramUpdateContext context)
  ```
- une commande doit être en minuscules ;
- une commande doit commencer par `/` ;
- une commande doit respecter le format :
  ```text
  /[a-z0-9_]{1,32}
  ```
- les doublons de handlers `@Command` provoquent une erreur ;
- les doublons de handlers `@CallbackQuery` provoquent une erreur ;
- un seul handler `@Chat` est autorisé par scope de bot.


---

## Licence
This project is licensed under the Apache License 2.0 with Commons Clause.

✔ You are allowed to:

* Use the software for personal or internal business use
* Modify and distribute it
* Contribute to the project

❌ You are NOT allowed to:

* Sell this software
* Sell a product or service primarily based on this software

See the LICENSE file for full details.