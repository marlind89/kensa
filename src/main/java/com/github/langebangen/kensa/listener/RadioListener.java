package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.AudioStreamer;
import com.github.langebangen.kensa.listener.event.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Martin.
 */
public class RadioListener
		extends AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(RadioListener.class);

	@Inject
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
	public void handleTrackStartEvent(TrackStartEvent event)
	{
		client.changeStatus(Status.game(event.getTrack().toString()));
	}

	@EventSubscriber
	public void handleTrackFinishEvent(TrackFinishEvent event)
	{
		if(event.getNewTrack().isPresent() == false)
		{
			client.changeStatus(Status.empty());
		}
	}

	@EventSubscriber
	public void handleSearchYoutubeEvent(SearchYoutubeEvent event)
	{
		try
		{
			ProcessBuilder info = new ProcessBuilder(
					"youtube-dl",
					"ytsearch8:" + event.getSearchQuery(),
					"-q",
					"-j",
					"--flat-playlist",
					"--ignore-errors",
					"--skip-download"
			);

			Process infoProcess = info.start();
			byte[] infoData = IOUtils.toByteArray(infoProcess.getInputStream());

			String sInfo = new String(infoData, Charset.forName("ISO-8859-1"));
			Scanner scanner = new Scanner(sInfo);
			JsonParser parser = new JsonParser();

			List<String> youtubeIds = new ArrayList<>();
			while(scanner.hasNextLine())
			{
				JsonObject json = parser.parse(scanner.nextLine()).getAsJsonObject();
				youtubeIds.add(json.get("id").getAsString());
			}

			List<String> commandList = new LinkedList<>(Arrays.asList("youtube-dl",
					"-q", "--ignore-errors",
					"--skip-download", "-e",
					"--get-duration",
					"--"));
			commandList.addAll(youtubeIds);
			Process titleFetcher = new ProcessBuilder(commandList).start();

			byte[] titlesBytes = IOUtils.toByteArray(titleFetcher.getInputStream());
			String titles = new String(titlesBytes, Charset.forName("ISO-8859-1"));
			scanner = new Scanner(titles);

			MessageBuilder messageBuilder = new MessageBuilder(client)
					.withChannel(event.getTextChannel())
					.appendContent("```");
			int i = 0;
			while(scanner.hasNextLine())
			{
				String youtubeId = youtubeIds.get(i++);
				String title = scanner.nextLine();
				String duration = scanner.nextLine();

				messageBuilder.appendContent(youtubeId);
				messageBuilder.appendContent(" - " + title + " [" + duration + "]\n");
			}
			messageBuilder.appendContent("```");
			sendMessage(messageBuilder);
		}
		catch(IOException e)
		{
			logger.error("Error when fetching information from " +
					"youtube-dl when perform a youtube search.", e);
		}
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
			MessageBuilder messageBuilder = new MessageBuilder(client)
					.withChannel(event.getTextChannel())
					.appendContent("```");

			int i = 1;
			for(AudioPlayer.Track track : playlist)
			{
				messageBuilder.appendContent(String.format("\n %d. %s", i++, track));
			}
			messageBuilder.appendContent("```");
			sendMessage(messageBuilder);
		}
	}

	@EventSubscriber
	public void handleClearPlaylistEvent(ClearPlaylistEvent event)
	{
		event.getPlayer().clear();
		sendMessage(event.getTextChannel(), "Playlist cleared.");
	}

	@EventSubscriber
	public void handlePauseEvent(PauseEvent event)
	{
		AudioPlayer player = event.getPlayer();
		IChannel channel = event.getTextChannel();

		String shouldPause = event.shouldPause() == null
				? ""
				: event.shouldPause();
		switch(shouldPause)
		{
			case "on":
				player.setPaused(true);
				break;
			case "off":
				player.setPaused(false);
				break;
			default:
				sendMessage(channel, "Invalid pause command. Specify on or off, e.g. \"!pause on\"");
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
}