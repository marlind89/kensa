package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.listener.event.*;
import com.github.langebangen.kensa.sentence.SentenceGenerator;
import com.github.langebangen.kensa.storage.Storage;
import com.github.langebangen.kensa.storage.generated.tables.records.InsultRecord;
import com.github.langebangen.kensa.util.KensaConstants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.github.langebangen.kensa.storage.generated.Tables.INSULT;


/**
 * @author Martin.
 */
@Singleton
public class TextChannelListener
    extends AbstractEventListener
{
    private static final Logger logger = LoggerFactory.getLogger(TextChannelListener.class);

    private final Babylon babylon;
    private final Storage storage;
    private final VoiceConnections voiceConnections;
    private final SentenceGenerator sentenceGenerator;

    private int lastInsultId;

    @Inject
    public TextChannelListener(GatewayDiscordClient client,
        Babylon babylon, Storage storage,
        VoiceConnections voiceConnections,
        SentenceGenerator sentenceGenerator)
    {
        super(client);

        this.babylon = babylon;
        this.storage = storage;
        this.voiceConnections = voiceConnections;
        this.sentenceGenerator = sentenceGenerator;
        this.lastInsultId = -1;

        onHelpEvent();
        onBabylonEvent();
        onMentionEvent();
        onInsultEvent();
        onInsultPersistEvent();
        onRestartKensaEvent();
    }

    private void onHelpEvent()
    {
        subscribe(HelpEvent.class, c -> c
            .flatMap(event -> event.getTextChannel().createEmbed(spec ->
            {
                spec.setAuthor("Kensa v" + KensaConstants.VERSION, "https://github.com/langebangen/kensa", null);
                spec.setTitle("Available commands:");

                for (Action action : Action.values())
                {
                    spec.addField(action.getAction(), action.getDescription(), false);
                }
            })));
    }


    private void onBabylonEvent()
    {
        subscribe(BabylonEvent.class, c -> c
            .flatMap(event -> event.getTextChannel()
                .createMessage("```" + babylon.getRandomDish() + "```")));
    }

    private void onMentionEvent()
    {
        subscribe(MessageCreateEvent.class, c -> c
            .flatMap(event -> event.getGuild()
                .map(guild -> guild.getClient().getSelfId())
                .filter(botId -> event.getMessage().getUserMentionIds().contains(botId))
                .flatMap(botId -> event.getMessage().getChannel())
                .flatMap(channel -> Mono.fromFuture(sentenceGenerator.generateSentence().exceptionally(x -> ""))
                    .flatMap(sentence -> sentence.isEmpty()
                        ? Mono.empty()
                        : channel.createMessage(sentence))
                )
            ));
    }

    private void onInsultEvent()
    {
        subscribe(InsultEvent.class, c -> c
            .flatMap(event ->
            {
                try (Connection conn = storage.getConnection())
                {
                    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
                    try (var stream = create.select()
                        .from(INSULT)
                        .orderBy(DSL.rand())
                        .stream())
                    {
                        var first = stream.findFirst();
                        if (first.isPresent())
                        {
                            var record = first.get();
                            String text = record.getValue(INSULT.TEXT);
                            lastInsultId = record.getValue(INSULT.ID);

                            return event.getTextChannel().createMessage(
                                event.getUser().getMention() + ", " + text);
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("Error when fetching insult from storage.", e);
                }
                return Mono.empty();
            }));
    }


    private void onInsultPersistEvent()
    {
        subscribe(InsultPersistEvent.class, c -> c
            .flatMap(event ->
            {
                if (!event.isAdded() && lastInsultId == -1)
                {
                    return event.getTextChannel()
                        .createMessage("No previous insult to remove!");
                }

                try (var conn = storage.getConnection())
                {
                    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
                    if (event.isAdded())
                    {
                        String insult = event.getInsult();
                        if (insult != null && !insult.isEmpty())
                        {
                            InsultRecord insultRecord = create.newRecord(INSULT);
                            insultRecord.setText(event.getInsult());
                            insultRecord.store();
                            return event.getTextChannel().createMessage("Insult added.");
                        }
                    }
                    else
                    {
                        create.delete(INSULT)
                            .where(INSULT.ID.equal(lastInsultId))
                            .execute();
                        lastInsultId = -1;
                        return event.getTextChannel().createMessage("Removed previous insult.");
                    }
                }
                catch (SQLException e)
                {
                    logger.error("Error when persisting insult.", e);
                }
                return Mono.empty();
            }));
    }


    public void onRestartKensaEvent()
    {
        subscribe(RestartKensaEvent.class, c -> c
            .flatMap(event -> voiceConnections.disconnect(event.getTextChannel().getGuildId())
                .defaultIfEmpty(null)
                .flatMap(vcc ->
                {
                    String voiceChannelId = vcc == null ? "" : " " + vcc.audioChannel().getId().asLong();
                    return event.getTextChannel().createMessage("Restarting...")
                        .then(event.getClient().logout())
                        .thenReturn(voiceChannelId);
                }))
            .doOnNext(voiceChannelId ->
            {
                List<String> command = new ArrayList<>();
                command.add("/bin/bash");
                command.add("-c");
                command.add("sleep 5 && ./kensa.sh" + voiceChannelId);
                ProcessBuilder builder = new ProcessBuilder(command);

                try
                {
                    builder.start();
                    System.exit(0);
                }
                catch (IOException e)
                {
                    logger.error("Failed to restart Kensa!", e);
                }
            }));
    }
}