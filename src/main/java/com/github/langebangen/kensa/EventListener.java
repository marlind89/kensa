package com.github.langebangen.kensa;

import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.listener.AbstractEventListener;
import com.github.langebangen.kensa.listener.event.*;
import com.github.langebangen.kensa.util.KensaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

import java.util.Random;

/**
 * EventListener which listens on events from discord.
 *
 * @author langen
 */
public class EventListener
	extends AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

	private final Random random;

	/**
	 * Constructor.
	 *
	 * @param client
	 *      the {@link IDiscordClient}
	 */
	public EventListener(IDiscordClient client)
	{
		super(client);
		this.random = new Random();
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
		IChannel textChannel = message.getChannel();

		Command command = Command.parseCommand(content);
		if(command != null)
		{
			String argument = command.getArgument();
			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
			EventDispatcher dispatcher = client.getDispatcher();
			switch(command.getAction())
			{
				/* Text channel commands */
				case HELP:
					dispatcher.dispatch(new HelpEvent(textChannel));
					break;
				case BABYLON:
					dispatcher.dispatch(new BabylonEvent(textChannel));
					break;

				/* Voice channel commands */
				case JOIN:
					dispatcher.dispatch(new JoinVoiceChannelEvent(textChannel, argument));
					break;
				case LEAVE:
					dispatcher.dispatch(new LeaveVoiceChannelEvent(textChannel));
					break;

				/* Radio commands */
				case PLAY:
					dispatcher.dispatch(new PlayAudioEvent(textChannel, player, argument));
					break;
				case SKIP:
					dispatcher.dispatch(new SkipTrackEvent(textChannel, player, argument));
					break;
				case SONG:
					dispatcher.dispatch(new CurrentTrackRequestEvent(textChannel, player));
					break;
				case LOOP:
					dispatcher.dispatch(new LoopPlaylistEvent(textChannel, player, argument));
					break;
				case SHUFFLE:
					dispatcher.dispatch(new ShufflePlaylistEvent(textChannel, player));
					break;
				case PLAYLIST:
					dispatcher.dispatch(new ShowPlaylistEvent(textChannel, player));
					break;
				case PAUSE:
					dispatcher.dispatch(new PauseEvent(textChannel, player, argument));
					break;
				case SEARCH:
					dispatcher.dispatch(new SearchYoutubeEvent(textChannel, argument));
					break;
				case CLEAR:
					dispatcher.dispatch(new ClearPlaylistEvent(textChannel, player));
					break;
			}
		}
		else
		{
			if((random.nextFloat() * 100) > 99)
			{
				sendMessage(textChannel, "YEAH, " + message.getContent().toUpperCase());
			}
		}
	}
}