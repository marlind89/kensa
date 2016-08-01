package com.github.langebangen.kensa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import java.util.List;

/**
 * EventListener which listens on events from discord.
 *
 * @author langen
 */
public class EventListener
{
	private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

	private final IDiscordClient client;

	/**
	 * Constructor.
	 *
	 * @param client
	 *      the {@link IDiscordClient}
	 */
	public EventListener(IDiscordClient client)
	{
		this.client = client;
	}

	/**
	 * Event received when the bot has succesfully logged in and ready.
	 *
	 * @param event
	 *      the {@link ReadyEvent}
	 */
	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		logger.info("Logged in successfully.!");
	}

	/**
	 * Event which is received when a message is sent in a guild
	 * this bot is connected to.
	 *
	 * @param event
	 *      the {@link MessageReceivedEvent
	 */
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event)
	{
		IMessage message = event.getMessage();
		String content = message.getContent();

		Command command = Command.parseCommand(content);
		if(command != null)
		{
			IGuild guild = message.getGuild();
			MessageBuilder messageBuilder = new MessageBuilder(client).withChannel(message.getChannel());
			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
			switch(command.getAction())
			{
				case HELP:
					sendHelpMessage(messageBuilder);
					break;
				case JOIN:
					String channel = command.getArgument();
					List<IVoiceChannel> voiceChannels = guild.getVoiceChannelsByName(channel);
					if(voiceChannels.isEmpty())
					{
						sendMessage(messageBuilder, "No channel with name " + channel + " exists!");
					}
					else
					{
						try
						{
							voiceChannels.get(0).join();
						}
						catch(MissingPermissionsException e)
						{
							sendMessage(messageBuilder, "I have no permission to join this channel :frowning2:");
						}
					}
					break;
				case LEAVE:
					client.getConnectedVoiceChannels().forEach(IVoiceChannel::leave);
					break;
				case PLAY:
					String url = command.getArgument();
					AudioStreamer.stream(url, AudioPlayer.getAudioPlayerForGuild(guild), messageBuilder);
					break;
				case SKIP:
					String indexString = command.getArgument();
					if(indexString == null)
					{
						//Skip current song
						player.skip();
					}
					else
					{
						int index = Integer.parseInt(indexString);
						player.skipTo(index);
					}
					break;
				case SONG:
					messageBuilder.appendContent("Current song: ")
							.appendContent(player.getCurrentTrack().toString(), MessageBuilder.Styles.BOLD);
					sendMessage(messageBuilder);
					break;
				case PLAYLIST:
					List<AudioPlayer.Track> playlist = player.getPlaylist();
					if(playlist.isEmpty())
					{
						sendMessage(messageBuilder, "No songs added to the playlist.");
					}
					else
					{
						int i = 1;
						for(AudioPlayer.Track track : playlist)
						{
							messageBuilder.appendContent(String.format("\n %d . %s", i++, track));
						}
						sendMessage(messageBuilder);
					}
					break;
				case CLEAR:
					player.clear();
					sendMessage(messageBuilder, "Playlist cleared.");
					break;
			}
		}
	}

	/**
	 * Sends the help message.
	 *
	 * @param messageBuilder
	 *      the {@link MessageBuilder}
	 */
	private void sendHelpMessage(MessageBuilder messageBuilder)
	{
		for(Action action : Action.values())
		{
			messageBuilder.appendContent("\n" + action.getAction(), MessageBuilder.Styles.BOLD);
			messageBuilder.appendContent(" - " + action.getDescription());
		}
		sendMessage(messageBuilder);
	}

	/**
	 * Sends the specified message.
	 *
	 * @param messageBuilder
	 *      the {@link MessageBuilder}
	 * @param message
	 *      the message
	 */
	private void sendMessage(MessageBuilder messageBuilder, String message)
	{
		messageBuilder.withContent(message);
		sendMessage(messageBuilder);
	}

	/**
	 * Sends the message which has been created in the
	 * specified {@link MessageBuilder}
	 *
	 * @param messageBuilder
	 *      the {@link MessageBuilder}
	 */
	private void sendMessage(MessageBuilder messageBuilder)
	{
		try
		{
			messageBuilder.send();
		}
		catch(DiscordException | RateLimitException | MissingPermissionsException e)
		{
			e.printStackTrace();
		}
	}
}