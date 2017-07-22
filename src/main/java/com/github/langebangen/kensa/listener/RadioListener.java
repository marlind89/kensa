package com.github.langebangen.kensa.listener;

import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.audio.MusicPlayerManager;
import com.github.langebangen.kensa.listener.event.ClearPlaylistEvent;
import com.github.langebangen.kensa.listener.event.CurrentTrackRequestEvent;
import com.github.langebangen.kensa.listener.event.KensaEvent;
import com.github.langebangen.kensa.listener.event.LoopPlaylistEvent;
import com.github.langebangen.kensa.listener.event.PauseEvent;
import com.github.langebangen.kensa.listener.event.PlayAudioEvent;
import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.github.langebangen.kensa.listener.event.ShowPlaylistEvent;
import com.github.langebangen.kensa.listener.event.ShufflePlaylistEvent;
import com.github.langebangen.kensa.listener.event.SkipTrackEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.vdurmont.emoji.Emoji;

/**
 * @author Martin.
 */
public class RadioListener
	extends AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(RadioListener.class);
	private final MusicPlayerManager playerFactory;

	@Inject
	public RadioListener(IDiscordClient client, MusicPlayerManager playerFactory)
	{
		super(client);
		this.playerFactory = playerFactory;
	}

	@EventSubscriber
	public void handlePlayAudioEvent(PlayAudioEvent event)
	{
		getPlayer(event).stream(event);
	}

	@EventSubscriber
	public void handleSearchYoutubeEvent(SearchYoutubeEvent event)
	{
		getPlayer(event).searchYoutube(event);
	}

	@EventSubscriber
	public void handleSkipTrackEvent(SkipTrackEvent event)
	{
		String skipAmountString = event.getSkipAmount();
		MusicPlayer musicPlayer = getPlayer(event);
		if(skipAmountString == null)
		{
			//Skip current song
			musicPlayer.skipTrack();
		}
		else if(!isInteger(skipAmountString))
		{
			sendMessage(event.getTextChannel(), "That's not a valid number!");
		}
		else
		{
			int skipAmount = Integer.parseInt(skipAmountString);
			musicPlayer.skipTrack(skipAmount);
		}
	}

	@EventSubscriber
	public void handleCurrentTrackRequestEvent(CurrentTrackRequestEvent event)
	{
		AudioTrack currentSong = getPlayer(event).getCurrentTrack();
		sendMessage(new MessageBuilder(client)
			.withChannel(event.getTextChannel())
			.appendContent("Current song: ")
			.appendContent(currentSong != null
				? TrackUtils.getReadableTrack(currentSong)
				: "none", MessageBuilder.Styles.BOLD));
	}

	@EventSubscriber
	public void handleLoopPlaylistEvent(LoopPlaylistEvent event)
	{
		IChannel channel = event.getTextChannel();
		IGuild guild = channel.getGuild();
		String loopEnabled = event.getLoopEnabled() == null
				? ""
				: event.getLoopEnabled();
		MusicPlayer musicPlayer = getPlayer(event);
		switch(loopEnabled)
		{
			case "on":
				musicPlayer.setLoopEnabled(true);
				sendMessage(channel, "Looping enabled.");
				break;
			case "off":
				musicPlayer.setLoopEnabled(false);
				sendMessage(channel, "Looping disabled.");
				break;
			default:
				sendMessage(channel, "Invalid loop command. Specify on or off, e.g. \"!loop on\"");
		}
	}

	@EventSubscriber
	public void handleShuffleEvent(ShufflePlaylistEvent event)
	{
		getPlayer(event).shuffle();
		sendMessage(event.getTextChannel(), "Playlist shuffled!");
	}

	@EventSubscriber
	public void handleShowPlaylistEvent(ShowPlaylistEvent event)
	{
		List<AudioTrack> playlist = getPlayer(event).getPlayList();
		if(playlist.isEmpty())
		{
			sendMessage(event.getTextChannel(), "No songs added to the playlist.");
		}
		else
		{
			MessageBuilder messageBuilder = new MessageBuilder(client)
					.withChannel(event.getTextChannel())
					.appendContent("```");

			int i = 1;
			String moreSongs = " \n and %d more...";
			for(AudioTrack track : playlist)
			{
				// The playlist size may be too large to send a message in
				// as the maximum message may be IMessage.MAX_MESSAGE_LENGTH characters long.
				// Reserving two digits for the amount of songs
				String trackString = String.format("\n %d. %s", i++, TrackUtils.getReadableTrack(track));
				if((trackString.length() + messageBuilder.getContent().length()) <
					(IMessage.MAX_MESSAGE_LENGTH - moreSongs.length() - 2))
				{
					messageBuilder.appendContent(trackString);
				}
				else
				{
					// We have reached the limit, print out the more songs string
					messageBuilder.appendContent(String.format(moreSongs, playlist.size() - i + 1));
					break;
				}
			}
			messageBuilder.appendContent("```");
			IMessage message = sendMessage(messageBuilder);

			RequestBuilder builder = new RequestBuilder(client);
			builder.shouldBufferRequests(true);

			builder.doAction(() -> addReaction(message,"⏯"))
				.andThen(() -> addReaction(message,"⏭"));

			builder.execute();
		}
	}

	@EventSubscriber
	public void handleClearPlaylistEvent(ClearPlaylistEvent event)
	{
		getPlayer(event).clearPlaylist();
		sendMessage(event.getTextChannel(), "Playlist cleared.");
	}

	@EventSubscriber
	public void handlePauseEvent(PauseEvent event)
	{
		MusicPlayer musicPlayer = getPlayer(event);
		IChannel channel = event.getTextChannel();
		String shouldPause = event.shouldPause() == null
			? ""
			: event.shouldPause();

		switch(shouldPause)
		{
			case "on":
				musicPlayer.pause(true);
				break;
			case "off":
				musicPlayer.pause( false);
				break;
			default:
				sendMessage(channel, "Invalid pause command. Specify on or off, e.g. \"!pause on\"");
		}
	}

	@EventSubscriber
	public void handleReactionEvent(ReactionEvent event)
	{
		if(!event.getUser().isBot())
		{
			Emoji emoji = event.getReaction().getUnicodeEmoji();
			if(emoji != null)
			{
				MusicPlayer player = playerFactory.getMusicPlayer(event.getGuild());
				switch(emoji.getUnicode())
				{
					case "⏯":
						player.pause(!player.isPaused());
						break;
					case "⏭":
						player.skipTrack();
						break;
				}
			}
		}
	}
	
	private boolean isInteger(String s)
	{
		return isInteger(s, 10);
	}

	private boolean isInteger(String s, int radix)
	{
		if(s.isEmpty())
		{
			return false;
		}
		for(int i = 0; i < s.length(); i++)
		{
			if(i == 0 && s.charAt(i) == '-')
			{
				if(s.length() == 1)
				{
					return false;
				}
				else
				{
					continue;
				}
			}
			if(Character.digit(s.charAt(i), radix) < 0)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the {@link MusicPlayer} associated with the specified {@link KensaEvent}
	 *
	 * @param event
	 * 		the {@link KensaEvent}
	 *
	 * @return
	 * 		the {@link MusicPlayer} associated with the specified {@link KensaEvent}
	 */
	private MusicPlayer getPlayer(KensaEvent event)
	{
		return playerFactory.getMusicPlayer(event);
	}

	private boolean addReaction(IMessage message, String reaction)
	{
		message.addReaction(reaction);
		return true;
	}
}