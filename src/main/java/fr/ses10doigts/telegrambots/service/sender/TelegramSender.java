package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramForumAccessCheck;
import fr.ses10doigts.telegrambots.model.TelegramForumTopic;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTopicIconColor;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;

public interface TelegramSender {

    /**
     * Envoie un message texte simple sans exposer son identifiant.
     */
    void sendMessage(Long chatId, String text);

    /**
     * Envoie un message texte simple et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMessageAndGetReference(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 échappé sans exposer son identifiant.
     */
    void sendMarkdownMessage(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 échappé et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 echappe dans un sujet precis d'un forum, sans
     * passer par le sujet courant du contexte d'execution.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    void sendMarkdownMessage(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie un message Markdown V2 echappe dans un sujet precis d'un forum et
     * retourne sa reference Telegram.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie un message Markdown V2 en préservant les liens sans exposer son identifiant.
     */
    void sendMarkdownMessagePreservingLinks(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 en préservant les liens et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 en preservant les liens dans un sujet precis d'un
     * forum, sans passer par le sujet courant du contexte d'execution.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    void sendMarkdownMessagePreservingLinks(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie un message Markdown V2 en preservant les liens dans un sujet precis d'un
     * forum et retourne sa reference Telegram.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie un message dont le texte MarkdownV2 est déjà entièrement préparé par
     * l'appelant (formatage volontaire : {@code *gras*}, {@code _italique_}, etc.),
     * sans aucun échappement automatique - contrairement à {@link #sendMarkdownMessage},
     * qui échappe tout le texte reçu et ne permet donc aucun formatage réel. Toute
     * partie dynamique/non maîtrisée du texte doit être échappée au préalable via
     * {@link TelegramMarkdownUtils#escapeMarkdownV2(String)}, sous peine d'erreur de
     * parsing Telegram ("can't find end of the entity...") si elle contient un des
     * caractères réservés MarkdownV2.
     */
    void sendFormattedMessage(Long chatId, String markdownV2Text);

    /**
     * Envoie un message MarkdownV2 déjà préparé par l'appelant (voir
     * {@link #sendFormattedMessage}) et retourne sa référence Telegram.
     */
    TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, String markdownV2Text);

    /**
     * Envoie un message MarkdownV2 deja prepare par l'appelant (voir
     * {@link #sendFormattedMessage}) dans un sujet precis d'un forum, sans passer par
     * le sujet courant du contexte d'execution.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    void sendFormattedMessage(Long chatId, Integer messageThreadId, String markdownV2Text);

    /**
     * Envoie un message MarkdownV2 deja prepare par l'appelant (voir
     * {@link #sendFormattedMessage}) dans un sujet precis d'un forum et retourne sa
     * reference Telegram.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, Integer messageThreadId, String markdownV2Text);

    /**
     * Édite le texte MarkdownV2 déjà préparé par l'appelant (voir
     * {@link #sendFormattedMessage}) d'un message existant.
     */
    TelegramMessageReference editFormattedMessage(Long chatId, Integer messageId, String markdownV2Text);

    /**
     * Envoie une photo sans exposer son identifiant.
     */
    void sendPhoto(Long chatId, String photoPath, String caption);

    /**
     * Envoie une photo et retourne sa référence Telegram.
     */
    TelegramMessageReference sendPhotoAndGetReference(Long chatId, String photoPath, String caption);

    /**
     * Envoie un document texte généré à la volée sans exposer son identifiant.
     */
    void sendTextDocument(Long chatId, String content, String fileName, String caption);

    /**
     * Envoie un document texte généré à la volée et retourne sa référence Telegram.
     */
    TelegramMessageReference sendTextDocumentAndGetReference(Long chatId, String content, String fileName, String caption);

    /**
     * Envoie un document sans exposer son identifiant.
     */
    void sendDocument(Long chatId, String documentPath, String caption);

    /**
     * Envoie un document et retourne sa référence Telegram.
     */
    TelegramMessageReference sendDocumentAndGetReference(Long chatId, String documentPath, String caption);

    /**
     * Envoie une vue Telegram sans exposer son identifiant.
     */
    void sendView(Long chatId, TelegramView view);

    /**
     * Envoie une vue Telegram et retourne sa référence Telegram.
     */
    TelegramMessageReference sendViewAndGetReference(Long chatId, TelegramView view);

    /**
     * Édite le texte d'un message existant.
     */
    TelegramMessageReference editMessage(Long chatId, Integer messageId, String text);

    /**
     * Édite le texte Markdown V2 d'un message existant.
     */
    TelegramMessageReference editMarkdownMessage(Long chatId, Integer messageId, String text);

    /**
     * Édite le texte Markdown V2 d'un message existant en préservant les liens.
     */
    TelegramMessageReference editMarkdownMessagePreservingLinks(Long chatId, Integer messageId, String text);

    /**
     * Édite une vue Telegram existante.
     */
    TelegramMessageReference editView(Long chatId, Integer messageId, TelegramView view);

    /**
     * Supprime un message existant.
     */
    boolean deleteMessage(Long chatId, Integer messageId);

    /**
     * Envoie une action de présence Telegram.
     */
    void sendChatAction(Long chatId, TelegramTypingAction action);

    /**
     * Envoie une action de présence Telegram dans un sujet précis d'un forum, sans
     * passer par le sujet courant du contexte d'exécution.
     *
     * @param messageThreadId identifiant du sujet visé, ou {@code null} pour cibler
     *                        hors de tout sujet (sujet "Général")
     */
    void sendChatAction(Long chatId, Integer messageThreadId, TelegramTypingAction action);

    /**
     * Déclenche l'indication "est en train d'écrire".
     */
    void sendTyping(Long chatId);

    /**
     * Déclenche l'indication "est en train d'écrire" dans un sujet précis d'un forum,
     * sans passer par le sujet courant du contexte d'exécution - utile depuis un
     * thread d'exécution qui n'est pas celui du traitement de l'update Telegram entrant
     * (ex. heartbeat périodique lancé sur un thread dédié pendant un appel bloquant),
     * où le sujet courant ne peut pas être résolu automatiquement (voir
     * ContextAwareTelegramSender, lié à un ThreadLocal).
     *
     * @param messageThreadId identifiant du sujet visé, ou {@code null} pour cibler
     *                        hors de tout sujet (sujet "Général")
     */
    void sendTyping(Long chatId, Integer messageThreadId);

    /**
     * Acquitte une callback query Telegram.
     */
    void answerCallbackQuery(String callbackQueryId);

    // ---------------------------------------------------------------------
    // Envoi cible dans un sujet (topic) precis d'un forum
    // ---------------------------------------------------------------------

    /**
     * Envoie un message texte dans un sujet precis d'un forum, sans passer par le
     * sujet courant du contexte d'execution.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    void sendMessage(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie un message texte dans un sujet precis d'un forum et retourne sa reference.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    TelegramMessageReference sendMessageAndGetReference(Long chatId, Integer messageThreadId, String text);

    /**
     * Envoie une vue Telegram dans un sujet precis d'un forum, sans passer par le
     * sujet courant du contexte d'execution.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    void sendView(Long chatId, Integer messageThreadId, TelegramView view);

    /**
     * Envoie une vue Telegram dans un sujet precis d'un forum et retourne sa reference.
     *
     * @param messageThreadId identifiant du sujet vise, ou {@code null} pour envoyer
     *                        hors de tout sujet (sujet "General")
     */
    TelegramMessageReference sendViewAndGetReference(Long chatId, Integer messageThreadId, TelegramView view);

    // ---------------------------------------------------------------------
    // Gestion des sujets (topics) d'un forum
    // ---------------------------------------------------------------------

    /**
     * Verifie si la gestion des sujets est possible dans ce groupe : "Sujets" actives
     * cote Telegram et droits d'administrateur du bot.
     *
     * <p>A utiliser avant de tenter {@link #createForumTopic}, {@link #editForumTopic}
     * etc. pour pouvoir afficher un message d'erreur clair plutot que de laisser
     * remonter une {@link fr.ses10doigts.telegrambots.exception.TelegramForumException}.
     * Effectue 2 appels Telegram ({@code getChat} + {@code getChatMember}).</p>
     *
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si la
     *         verification elle-meme echoue (groupe inaccessible, etc.)
     */
    TelegramForumAccessCheck checkForumAccess(Long chatId);

    /**
     * Raccourci de {@link #checkForumAccess} : {@code true} si le bot peut creer,
     * editer, fermer ou rouvrir un sujet dans ce groupe.
     */
    boolean canManageForumTopics(Long chatId);

    /**
     * Cree un sujet dans un forum (supergroupe avec "Sujets" actives), sans icone.
     *
     * @param name titre du sujet, 1 a 128 caracteres
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         groupe n'est pas un forum, si le bot n'a pas le droit
     *         "Gerer les sujets", ou en cas d'erreur Telegram
     */
    TelegramForumTopic createForumTopic(Long chatId, String name);

    /**
     * Cree un sujet dans un forum (supergroupe avec "Sujets" actives).
     *
     * @param name              titre du sujet, 1 a 128 caracteres
     * @param iconColor         couleur unie de l'icone, ou {@code null} pour la
     *                          couleur par defaut choisie par Telegram
     * @param iconCustomEmojiId identifiant d'un emoji personnalise a utiliser comme
     *                          icone, ou {@code null}
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         groupe n'est pas un forum, si le bot n'a pas le droit
     *         "Gerer les sujets", ou en cas d'erreur Telegram
     */
    TelegramForumTopic createForumTopic(
            Long chatId,
            String name,
            TelegramTopicIconColor iconColor,
            String iconCustomEmojiId
    );

    /**
     * Edite le titre et/ou l'icone (emoji personnalise) d'un sujet existant.
     *
     * <p>La couleur unie de l'icone n'est pas modifiable apres la creation
     * (limitation de l'API Telegram).</p>
     *
     * @param name              nouveau titre (1 a 128 caracteres), ou {@code null}
     *                          pour ne pas le changer
     * @param iconCustomEmojiId nouvel emoji personnalise, chaine vide pour retirer
     *                          l'emoji, ou {@code null} pour ne pas le changer
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         sujet n'existe pas, si le bot n'a pas le droit "Gerer les sujets",
     *         ou en cas d'erreur Telegram
     */
    boolean editForumTopic(Long chatId, Integer messageThreadId, String name, String iconCustomEmojiId);

    /**
     * Ferme un sujet (reste visible et consultable, mais plus de nouveaux messages).
     *
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         sujet n'existe pas, si le bot n'a pas le droit "Gerer les sujets",
     *         ou en cas d'erreur Telegram
     */
    boolean closeForumTopic(Long chatId, Integer messageThreadId);

    /**
     * Rouvre un sujet precedemment ferme.
     *
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         sujet n'existe pas, si le bot n'a pas le droit "Gerer les sujets",
     *         ou en cas d'erreur Telegram
     */
    boolean reopenForumTopic(Long chatId, Integer messageThreadId);

    /**
     * Supprime definitivement un sujet et tous les messages qu'il contient.
     *
     * <p>Necessite le droit "Supprimer les messages" en plus de "Gerer les sujets"
     * cote Telegram. Irreversible.</p>
     *
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         sujet n'existe pas, si le bot n'a pas les droits requis, ou en cas
     *         d'erreur Telegram
     */
    boolean deleteForumTopic(Long chatId, Integer messageThreadId);

    /**
     * Desepingle tous les messages epingles d'un sujet.
     *
     * @throws fr.ses10doigts.telegrambots.exception.TelegramForumException si le
     *         sujet n'existe pas, si le bot n'a pas les droits requis, ou en cas
     *         d'erreur Telegram
     */
    boolean unpinAllForumTopicMessages(Long chatId, Integer messageThreadId);

}
