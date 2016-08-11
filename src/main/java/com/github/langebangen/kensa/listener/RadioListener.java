package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.AudioStreamer;
import com.github.langebangen.kensa.listener.event.*;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

import java.util.List;

/**
 * @author Martin.
 */
public class RadioListener
		extends AbstractEventListener
{
	public RadioListener(IDiscordClient client)
	{
		super(client);
	}

	@EventSubscriber
	public void handlePlayAudioEvent(PlayAudioEvent event)
	{
		AudioStreamer.stream(event.getUrl(), event.getTextChannel());
	}

	@EventSubscriber
	public void handleSkipTrackEvent(SkipTrackEvent event)
	{
		String skipAmountString = event.getSkipAmount();
		AudioPlayer player = event.getPlayer();

		if(skipAmountString == null)
		{
			//Skip current song
			player.skip();
		}
		else
		{
			if(isInteger(skipAmountString) == false)
			{
				sendMessage(event.getTextChannel(), "That's not a valid number!");
			}
			else
			{
				int skipAmount = Integer.parseInt(skipAmountString);
				player.skipTo(skipAmount);
			}
		}
	}

	@EventSubscriber
	public void handleCurrentTrackRequestEvent(CurrentTrackRequestEvent event)
	{
		AudioPlayer.Track currentTrack = event.getCurrentTrack();

		sendMessage(new MessageBuilder(client)
				.withChannel(event.getTextChannel())
				.appendContent("Current song: ")
				.appendContent(currentTrack != null ? event.getCurrentTrack().toString() : "none", MessageBuilder.Styles.BOLD));
	}

	@EventSubscriber
	public void handleLoopPlaylistEvent(LoopPlaylistEvent event)
	{
		IChannel channel = event.getTextChannel();
		AudioPlayer player = event.getPlayer();
		String loopEnabled = event.getLoopEnabled() == null
				? ""
				: event.getLoopEnabled();
		switch(loopEnabled)
		{
			case "on":
				player.setLoop(true);
				sendMessage(channel, "Looping enabled.");
				break;
			case "off":
				player.setLoop(false);
				sendMessage(channel, "Looping disabled.");
				break;
			default:
				sendMessage(channel, "Invalid loop command. Specify on or off, e.g. \"!loop on\"");
		}
	}

	@EventSubscriber
	public void handleShuffleEvent(ShufflePlaylistEvent event)
	{
		event.getPlayer().shuffle();
		sendMessage(event.getTextChannel(), "Playlist shuffled!");
	}

	@EventSubscriber
	public void handleShowPlaylistEvent(ShowPlaylistEvent event)
	{
		List<AudioPlayer.Track> playlist = event.getPlayer().getPlaylist();
		if(playlist.isEmpty())
		{
			sendMessage(event.getTextChannel(), "No songs added to the playlist.");
		}
		else
		{
			MessageBuilder messageBuilder = new MessageBuilder(client);
			int i = 1;
			for(AudioPlayer.Track track : playlist)
			{
				messageBuilder.appendContent(String.format("\n %d . %s", i++, track));
			}
			sendMessage(messageBuilder);
		}
	}

	@EventSubscriber
	public void handleClearPlaylistEvent(ClearPlaylistEvent event)
	{
		event.getPlayer().clear();
		sendMessage(event.getTextChannel(), "Playlist cleared.");
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
}
