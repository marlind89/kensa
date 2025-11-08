package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.listener.event.*;
import com.github.langebangen.kensa.storage.Storage;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Random;

import static com.github.langebangen.kensa.storage.generated.Tables.MESSAGE;

/**
 * EventListener which listens on events from discord.
 *
 * @author langen
 */
public class EventListener
    extends AbstractEventListener
{
    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    private static final String PUNCTUATIONS = ".!?";
    private final Random random;
    private final VoiceConnections voiceConnections;
    private final Storage storage;
    private final long latestVoiceChannelId;

    @Inject
    public EventListener(GatewayDiscordClient client,
        VoiceConnections voiceConnections,
        Storage storage,
        @Named("latestVoiceChannelId") long latestVoiceChannelId)
    {
        super(client);
        this.voiceConnections = voiceConnections;
        this.storage = storage;
        this.latestVoiceChannelId = latestVoiceChannelId;
        this.random = new Random();

        onReady();
        onMessageReceivedEvent();
        onMemberJoinsVoiceChannel();
    }

    /**
     * Event received when the bot has successfully logged in and ready.
     */
    private void onReady()
    {
        subscribe(ReadyEvent.class, c -> c
            .take(1)
            .doOnNext(e -> logger.info("Logged in successfully!"))
            .filter(msg -> latestVoiceChannelId > 0)
            .flatMap(msg -> client.getChannelById(Snowflake.of(latestVoiceChannelId)))
            .ofType(VoiceChannel.class)
            .flatMap(voiceChannel ->
            {
                logger.info("Rejoining channel " + voiceChannel.getName());
                return voiceConnections.join(voiceChannel);
            }));
    }

    /**
     * Event which is received when a message is sent in a guild
     * this bot is connected to.
     */
    private void onMessageReceivedEvent()
    {
        Flux<Message> messageFlux = dispatcher.on(MessageCreateEvent.class)
            .map(MessageCreateEvent::getMessage);

        // Handles random YEAH event
        subscribe(messageFlux
            .filterWhen(event -> event.getAuthorAsMember().map(member -> !member.isBot()))
            .filter(message -> Command.parseCommand(message.getContent()) == null)
            .doOnNext(this::logMessage)
            .filter(message -> (random.nextFloat() * 1000) > 999)
            .flatMap(message -> message.getChannel()
                .flatMap(channel -> channel.createMessage("YEAH, " + message.getContent()))));

        subscribe(messageFlux
            .flatMap(message ->
            {
                Command command = Command.parseCommand(message.getContent());

                return Mono.zip(Mono.justOrEmpty(command),
                    message.getAuthorAsMember().flatMap(member -> command.getAction().hasPermission(member)),
                    message.getGuild(),
                    message.getChannel().ofType(TextChannel.class),
                    message.getAuthorAsMember());
            })
            .flatMap(zip ->
            {

                Command command = zip.getT1();
                boolean hasPermission = zip.getT2();
                Guild guild = zip.getT3();
                TextChannel channel = zip.getT4();
                Member member = zip.getT5();
                String argument = command.getArgument();

                if (!hasPermission)
                {
                    channel.createMessage("You don't have permission do to that, you filthy fool!").subscribe();
                    return Mono.empty();
                }

                return switch (command.getAction())
                {
                    /* Text channel commands */
                    case HELP -> Mono.just(new HelpEvent(client, channel));
                    case BABYLON -> Mono.just(new BabylonEvent(client, channel));
                    case INSULT ->
                    {
                        String[] insultArgs = argument.split(" ");
                        String insultType = insultArgs[0];
                        if (insultType.startsWith("<"))
                        {
                            String userId = insultType.replaceAll("[^\\d]", "");
                            yield guild.getMemberById(Snowflake.of(Long.parseLong(userId)))
                                .map(user -> new InsultEvent(client, channel, user));
                        }
                        else if (insultType.equals("add"))
                        {
                            String insult = StringUtils.join(Arrays.copyOfRange(insultArgs, 1, insultArgs.length), " ");
                            yield Mono.just(new InsultPersistEvent(client, channel, true, insult));
                        }
                        else if (insultType.equals("remove"))
                        {
                            yield Mono.just(new InsultPersistEvent(client, channel, false, null));
                        }
                        yield Mono.empty();
                    }
                    /* Voice channel commands */
                    case JOIN -> Mono.just(new JoinVoiceChannelEvent(client, channel, argument, member));
                    case LEAVE -> Mono.just(new LeaveVoiceChannelEvent(client, channel));
                    case RECONNECT -> Mono.just(new ReconnectVoiceChannelEvent(client, channel));
                    /* Music player commands */
                    case PLAY ->
                    {
                        String playArg = argument.replace("-p ", "");
                        yield Mono.just(
                            new PlayAudioEvent(client, channel, playArg, !playArg.equals(argument), member, false));
                    }
                    case SKIP -> Mono.just(new SkipTrackEvent(client, channel, argument));
                    case SONG -> Mono.just(new CurrentTrackRequestEvent(client, channel));
                    case LOOP -> Mono.just(new LoopPlaylistEvent(client, channel, argument));
                    case SHUFFLE -> Mono.just(new ShufflePlaylistEvent(client, channel));
                    case PLAYLIST -> Mono.just(new ShowPlaylistEvent(client, channel));
                    case PAUSE -> Mono.just(new PauseEvent(client, channel, argument));
                    case SEARCH ->
                    {
                        if (argument != null)
                        {
                            var searchArg = argument.replace("-p ", "");
                            if (!searchArg.trim().isEmpty())
                            {
                                yield Mono.just(
                                    new SearchYoutubeEvent(client, channel, searchArg, !searchArg.equals(argument)));
                            }
                        }
                        yield Mono.empty();
                    }
                    case CLEAR -> Mono.just(new ClearPlaylistEvent(client, channel));
                    /* Misc commands */
                    case RESTART -> Mono.just(new RestartKensaEvent(client, channel));
                    default -> Mono.empty();
                };
            })
            .doOnNext(dispatcher::publish));
    }

    private void onMemberJoinsVoiceChannel()
    {
        subscribe(VoiceStateUpdateEvent.class, c -> c
            .filter(
                x -> x.getCurrent().getUserId().equals(client.getSelfId()) && x.getCurrent().getChannelId().isPresent())
            .switchMap(event ->
            {
                var currentVoiceChannelId = event.getCurrent().getChannelId().get();

                return dispatcher.on(VoiceStateUpdateEvent.class)
                    .filter(x ->
                    {
                        var current = x.getCurrent();
                        var old = x.getOld();

                        return current.getUserId().equals(Snowflake.of("144085745320198154")) &&
                            current.getChannelId()
                                .map(chId -> chId.equals(currentVoiceChannelId))
                                .orElse(false) &&
                            old
                                .flatMap(VoiceState::getChannelId)
                                .map(oldChId -> !oldChId.equals(currentVoiceChannelId))
                                .orElse(true);
                    })
                    .flatMap(x -> Mono.zip(
                        Mono.justOrEmpty(x.getCurrent().getGuildId()),
                        x.getCurrent().getMember()
                    ));
            })
            .delayElements(Duration.ofMillis(500))
            .doOnNext(tuple ->
            {
                var guildId = tuple.getT1();
                var member = tuple.getT2();

                var rand = random.nextInt(3);
                String soundFile = switch (rand)
                {
                    case 0 -> "fredrik.mp3";
                    case 1 -> "fredrik2.mp3";
                    case 2 -> "hjalp.mp3";
                    default -> throw new IllegalStateException("Unexpected value: " + rand);
                };

                dispatcher.publish(new PlayAudioEvent(client, guildId,
                    soundFile, false, member, true));
            }));
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