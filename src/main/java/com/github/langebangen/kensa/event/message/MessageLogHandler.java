package com.github.langebangen.kensa.event.message;

import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.storage.Storage;
import com.google.inject.Inject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import org.apache.commons.validator.routines.UrlValidator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.github.langebangen.kensa.storage.generated.Tables.MESSAGE;

public class MessageLogHandler implements EventHandler<MessageCreateEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(MessageLogHandler.class);
    private static final String PUNCTUATIONS = ".!?";
    private final Storage storage;

    @Inject
    public MessageLogHandler(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public Publisher<?> handle(MessageCreateEvent event)
    {
        var message = event.getMessage();
        if (Command.parseCommand(message.getContent()) != null)
        {
            return Mono.empty();
        }

        return message.getAuthorAsMember()
            .filter(member -> !member.isBot())
            .doOnNext(e -> logMessage(message));
    }

    /**
     * Logs the message to db, used for generating random sentences.
     *
     * @param message the message
     */
    private void logMessage(Message message)
    {
        var content = message.getContent();
        StringBuilder sb = new StringBuilder();
        for (String word : content.split(" "))
        {
            if (!UrlValidator.getInstance().isValid(word)
                && !word.matches("<@!*\\d+>"))
            {
                sb.append(" ");
                sb.append(word);
            }
        }
        String urlFreeMessage = sb.toString();
        urlFreeMessage = urlFreeMessage.trim();
        if (!urlFreeMessage.isEmpty())
        {
            urlFreeMessage = formatSentence(urlFreeMessage);

            try (var conn = storage.getConnection())
            {
                DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
                var messageRecord = create.newRecord(MESSAGE);
                messageRecord.setText(urlFreeMessage);

                var zoneId = ZoneId.systemDefault();
                var utcOffset = ZonedDateTime.now(zoneId).getOffset();
                messageRecord.setSentAt(message.getTimestamp().atOffset(utcOffset));
                messageRecord.setAuthor(message.getAuthor().map(User::getUsername).orElse("??"));
                messageRecord.store();
            }
            catch (SQLException e)
            {
                logger.error("Error writing content to messages file.", e);
            }
        }
    }

    /**
     * Adds white spaces after dots and makes the character
     * after the dot and whitespace upper case.
     *
     * @param message the message to format
     * @return the formatted sentence
     */
    private static String formatSentence(String message)
    {
        // Make the first character upper case and append a dot
        // to the end of the string if there wasn't any.
        message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        if (message.charAt(message.length() - 1) != '.')
        {
            message += ".";
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = message.toCharArray();
        for (int i = 0;
             i < chars.length;
             i++)
        {
            char c = chars[i];
            sb.append(c);
            if (PUNCTUATIONS.contains("" + c) && i <= chars.length - 2)
            {
                char c2 = chars[++i];
                char c3;
                if (c2 != ' ')
                {
                    sb.append(' ');
                    c3 = c2;
                }
                else
                {
                    c3 = chars[++i];
                }
                sb.append(Character.toUpperCase(c3));
            }
        }
        return sb.toString();
    }
}
