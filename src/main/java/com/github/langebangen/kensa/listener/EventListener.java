package com.github.langebangen.kensa.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.inject.Named;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rita.RiMarkov;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.listener.event.BabylonEvent;
import com.github.langebangen.kensa.listener.event.ClearPlaylistEvent;
import com.github.langebangen.kensa.listener.event.CurrentTrackRequestEvent;
import com.github.langebangen.kensa.listener.event.HelpEvent;
import com.github.langebangen.kensa.listener.event.InsultEvent;
import com.github.langebangen.kensa.listener.event.InsultPersistEvent;
import com.github.langebangen.kensa.listener.event.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LeaveVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LoopPlaylistEvent;
import com.github.langebangen.kensa.listener.event.PauseEvent;
import com.github.langebangen.kensa.listener.event.PlayAudioEvent;
import com.github.langebangen.kensa.listener.event.ReconnectVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.RestartKensaEvent;
import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.github.langebangen.kensa.listener.event.ShowPlaylistEvent;
import com.github.langebangen.kensa.listener.event.ShufflePlaylistEvent;
import com.github.langebangen.kensa.listener.event.SkipTrackEvent;
import com.google.inject.Inject;

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
	private final File messageFile;
	private final Random random;
	private final RiMarkov markov;
	private final VoiceConnections voiceConnections;
	private long latestVoiceChannelId;

	@Inject
	public EventListener(GatewayDiscordClient client,
		RiMarkov markov,
		VoiceConnections voiceConnections,
		@Named("latestVoiceChannelId") long latestVoiceChannelId)
	{
		super(client);
		this.voiceConnections = voiceConnections;
		this.latestVoiceChannelId = latestVoiceChannelId;
		this.random = new Random();
		this.messageFile = new File("messages.txt");
		this.markov = markov;

		onReady();
		onMessageReceivedEvent();
		onReconnectEvent();
	}

	/**
	 * Event received when the bot has succesfully logged in and ready.
	 */
	private void onReady()
	{
		dispatcher.on(ReadyEvent.class)
			.take(1)
			.doOnNext(e -> logger.info("Logged in successfully!"))
			.filter(msg -> latestVoiceChannelId > 0)
			.flatMap(msg -> client.getChannelById(Snowflake.of(latestVoiceChannelId)))
			.ofType(VoiceChannel.class)
			.flatMap(voiceChannel -> {
				logger.info("Rejoining channel " + voiceChannel.getName());
				return voiceConnections.join(voiceChannel);
			})
			.subscribe();
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
		messageFlux
			.filterWhen(event -> event.getAuthorAsMember().map(member -> !member.isBot()))
			.filter(message -> Command.parseCommand(message.getContent()) == null)
			.doOnNext(message -> logMessage(message.getContent()))
			.filter(message -> (random.nextFloat() * 100) > 99)
			.flatMap(message -> message.getChannel()
				.flatMap(channel -> channel.createMessage("YEAH, " + message.getContent())))
			.subscribe();

		messageFlux
			.flatMap(message -> {
				Command command = Command.parseCommand(message.getContent());

				return Mono.zip(Mono.justOrEmpty(command),
					message.getAuthorAsMember().flatMap(member -> command.getAction().hasPermission(member)),
					message.getGuild(),
					message.getChannel().ofType(TextChannel.class),
					message.getAuthorAsMember());
			})
			.flatMap(zip -> {

				Command command = zip.getT1();
				boolean hasPermission = zip.getT2();
				Guild guild = zip.getT3();
				TextChannel channel = zip.getT4();
				Member member = zip.getT5();
				String argument = command.getArgument();

				if (!hasPermission){
					channel.createMessage("You don't have permission do to that, you filthy fool!").subscribe();
					return Mono.empty();
				}

				switch(command.getAction())
				{
					/* Text channel commands */
					case HELP:
						return Mono.just(new HelpEvent(client, channel));
					case BABYLON:
						return Mono.just(new BabylonEvent(client, channel));
					case INSULT:
						String[] insultArgs = argument.split(" ");
						String insultType = insultArgs[0];
						if(insultType.startsWith("<"))
						{
							String userId = insultType.replaceAll("[^\\d]", "");

							return guild.getMemberById(Snowflake.of(Long.parseLong(userId)))
								.map(user -> new InsultEvent(client, channel, user));
						}
						else if(insultType.equals("add"))
						{
							String insult = StringUtils
								.join(Arrays.copyOfRange(insultArgs, 1, insultArgs.length), " ");
							return Mono.just(new InsultPersistEvent(client, channel, true, insult));
						}
						else if(insultType.equals("remove"))
						{
							return Mono.just(new InsultPersistEvent(client, channel, false, null));
						}
					/* Voice channel commands */
					case JOIN:
						return Mono.just(new JoinVoiceChannelEvent(client, channel, argument, member));
					case LEAVE:
						return Mono.just(new LeaveVoiceChannelEvent(client, channel));
					case RECONNECT:
						return Mono.just(new ReconnectVoiceChannelEvent(client, channel));
					/* Radio commands */
					case PLAY:
						String playArg = argument.replace("-p ", "");
						return Mono.just(new PlayAudioEvent(client, channel, playArg, !playArg.equals(argument), member));
					case SKIP:
						return Mono.just(new SkipTrackEvent(client, channel, argument));
					case SONG:
						return Mono.just(new CurrentTrackRequestEvent(client, channel));
					case LOOP:
						return Mono.just(new LoopPlaylistEvent(client, channel, argument));
					case SHUFFLE:
						return Mono.just(new ShufflePlaylistEvent(client, channel));
					case PLAYLIST:
						return Mono.just(new ShowPlaylistEvent(client, channel));
					case PAUSE:
						return Mono.just(new PauseEvent(client, channel, argument));
					case SEARCH:
						String searchArg = argument.replace("-p ", "");
						return Mono.just(new SearchYoutubeEvent(client, channel, searchArg, !searchArg.equals(argument)));
					case CLEAR:
						return Mono.just(new ClearPlaylistEvent(client, channel));
					case RESTART:
						return Mono.just(new RestartKensaEvent(client, channel));
				}

				return Mono.empty();
			})
			.subscribe(dispatcher::publish);
	}

	private void onReconnectEvent()
	{
		Flux<GatewayDiscordClient> reconnectEvent = dispatcher.on(ReconnectEvent.class)
			.doOnNext(e -> logger.info("Received reconnect event")).map(Event::getClient);

		Flux<GatewayDiscordClient> readyEvent = dispatcher.on(ReadyEvent.class).skip(1)
			.doOnNext(e -> logger.info("Received new ready event")).map(Event::getClient);

		reconnectEvent.mergeWith(readyEvent)
			.flatMap(client -> client.getGuilds()
				.flatMap(guild -> guild.getMemberById(client.getSelfId())))
			.flatMap(Member::getVoiceState)
			.flatMap(VoiceState::getChannel)
			.flatMap(vc -> voiceConnections.reconnect(vc, false))
			.subscribe();
	}

	/**
	 * Logs the message to the message file.
	 * Will also update {@link RiMarkov} with the
	 * message.
	 *
	 * @param message
	 *      the message
	 */
	private void logMessage(String message)
	{
		StringBuilder sb = new StringBuilder();
		for(String word : message.split(" "))
		{
			if(!UrlValidator.getInstance().isValid(word)
					&& !word.matches("<@!*\\d+>"))
			{
				sb.append(" ");
				sb.append(word);
			}
		}
		String urlFreeMessage = sb.toString();
		urlFreeMessage = urlFreeMessage.trim();
		if(!urlFreeMessage.isEmpty())
		{
			urlFreeMessage = formatSentence(urlFreeMessage);
			markov.loadText(urlFreeMessage);
			try(FileWriter writer = new FileWriter(messageFile, true))
			{
				writer.write(urlFreeMessage);
			}
			catch(IOException e)
			{
				logger.error("Error writing message to messages file.", e);
			}
		}
	}

	/**
	 * Adds white spaces after dots and makes the character
	 * after the dot and whitespace upper case.
	 *
	 * @param message
	 *      the message to format
	 *
	 * @return
	 *      the formatted sentence
	 */
	private static String formatSentence(String message)
	{
		// Make the first character upper case and append a dot
		// to the end of the string if there wasn't any.
		message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
		if(message.charAt(message.length()-1) != '.')
		{
			message += ".";
		}

		StringBuilder sb = new StringBuilder();
		char[] chars = message.toCharArray();
		for(int i=0; i<chars.length; i++)
		{
			char c = chars[i];
			sb.append(c);
			if(PUNCTUATIONS.contains("" + c) && i <= chars.length-2)
			{
				char c2 = chars[++i];
				char c3;
				if(c2 != ' ')
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