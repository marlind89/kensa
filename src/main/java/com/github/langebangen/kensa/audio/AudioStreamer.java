package com.github.langebangen.kensa.audio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.providers.URLProvider;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Static utility class for streaming content from a specified url to the {@link AudioPlayer}
 *
 * @author langen
 */
public class AudioStreamer
{
	private static final Logger logger = LoggerFactory.getLogger(AudioStreamer.class);

	/**
	 * Streams the content located on the specified URL to the specified player.
	 * Will send a message that the content has been added to the playlist queue.
	 *
	 * @param urlString
	 *      the url string
	 * @param channel
	 *      the {@link IChannel}
	 */
	public static void stream(String urlString, IChannel channel)
	{
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(channel.getGuild());

		if(urlString.contains("youtube.com") || urlString.contains("youtu.be"))
		{
			try
			{
				streamYoutube(urlString, player, channel);
			}
			catch(UnsupportedAudioFileException | DiscordException | IOException
					| RateLimitException | MissingPermissionsException e)
			{
				logger.error("Error when streaming content from youtube.", e);
			}
		}
		else if(UrlValidator.getInstance().isValid(urlString))
		{
			try
			{
				URL url = new URL(urlString);
				TrackMeta trackMeta = new TrackMeta(TrackSource.URL, url.toString(), null, -1);
				ExtendedTrack track = new ExtendedTrack(new URLProvider(url), trackMeta);
				player.queue(track);
				sendPlayMessage(trackMeta, channel);
			}
			catch(UnsupportedAudioFileException | IOException e)
			{
				logger.error("Error when streaming content from url", e);
			}
		}
		else
		{
			String youtubeVideoId = getFirstYoutubeMatch(urlString);
			try
			{
				streamYoutube(youtubeVideoId, player, channel);
			}
			catch(UnsupportedAudioFileException | DiscordException | IOException
					| RateLimitException | MissingPermissionsException e)
			{
				logger.error("Error when streaming content from youtube.", e);
			}
		}
	}

	/**
	 * Gets the first video url match based id based on the search query
	 * from youtube.
	 *
	 * @param searchQuery
	 *      the youtube search query
	 *
	 * @return youtubeId
	 *      the youtube id of the video which was the first match
	 */
	private static String getFirstYoutubeMatch(String searchQuery)
	{
		ProcessBuilder info = new ProcessBuilder(
				"youtube-dl",
				"ytsearch:" + searchQuery,
				"-q",
				"-j",
				"--flat-playlist",
				"--ignore-errors",
				"--skip-download"
		);

		byte[] infoData = new byte[0];
		try
		{
			Process infoProcess = info.start();
			infoData = IOUtils.toByteArray(infoProcess.getInputStream());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(infoData == null || infoData.length == 0)
		{
			throw new NullPointerException("The youtube-dl info process returned no data!");
		}

		String sInfo = new String(infoData);
		Scanner scanner = new Scanner(sInfo);
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(scanner.nextLine()).getAsJsonObject();
		return json.get("id").getAsString();
	}

	/**
	 * Streams the content from an youtube url link.
	 *
	 * @param url
	 *      the url
	 * @param player
	 *      the {@link AudioPlayer}
	 * @param channel
	 *      the {@link IChannel}
	 *
	 * @throws MissingPermissionsException
	 * @throws IOException
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws UnsupportedAudioFileException
	 */
	private static void streamYoutube(String url, AudioPlayer player, IChannel channel)
			throws MissingPermissionsException, IOException, RateLimitException, DiscordException,
			UnsupportedAudioFileException
	{
		List<TrackMeta> trackMetas = new LinkedList<TrackMeta>();
		ProcessBuilder info = new ProcessBuilder(
				"youtube-dl",
				"-q",                   //quiet. No standard out.
				"-j",                   //Print JSON
				"--flat-playlist",      //Get ONLY the urls of the playlist if this is a playlist.
				"--ignore-errors",
				"--skip-download",
				"--", url
		);

		byte[] infoData = new byte[0];
		try
		{
			Process infoProcess = info.start();
			infoData = IOUtils.toByteArray(infoProcess.getInputStream());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(infoData == null || infoData.length == 0)
		{
			throw new NullPointerException("The youtube-dl info process returned no data!");
		}

		String sInfo = new String(infoData);
		Scanner scanner = new Scanner(sInfo);

		JsonParser parser = new JsonParser();
		if(url.contains("playlist"))
		{
			while(scanner.hasNextLine())
			{
				JsonObject json = parser.parse(scanner.nextLine()).getAsJsonObject();

				// Duration not available when getting meta from songs in a playlist
				trackMetas.add(new TrackMeta(TrackSource.YOUTUBE,
						json.get("url").getAsString(),
						json.get("title").getAsString(), -1));
			}
		}
		else
		{
			JsonObject json = parser.parse(scanner.nextLine()).getAsJsonObject();

			String title = json.has("title")
					? json.get("title").getAsString() : (json.has("fulltitle")
					? json.get("fulltitle").getAsString() : null);

			int durationInSeconds = json.has("duration") ? json.get("duration").getAsInt() : -1;

			trackMetas.add(new TrackMeta(TrackSource.YOUTUBE, url, title, durationInSeconds));
		}

		// Send queue message before queuing if its a playlist since
		// it can take some time before all tracks are queued.
		if(trackMetas.size() > 1)
		{
			new MessageBuilder(channel.getClient())
					.withChannel(channel)
					.appendContent("Queuing " + trackMetas.size() + " songs..")
					.build();
		}

		for(TrackMeta trackMeta : trackMetas)
		{
			ProcessBuilder youtube = new ProcessBuilder("youtube-dl",
				"-q",
				"-f", "bestaudio",
				"--exec", "ffmpeg -hide_banner -nostats -loglevel panic -y -i {} -vn -q:a 6 -f mp3 pipe:1",
				"-o", "%(id)s", "--", trackMeta.getUrl());

			Process yProcess = youtube.start();

			new Thread("youtube-dl ErrorStream")
			{
				@Override
				public void run()
				{
					try
					{
						InputStream fromYTDL = null;

						fromYTDL = yProcess.getErrorStream();
						if(fromYTDL == null)
						{
							logger.error("youtube-dl ErrorStream is null");
						}

						byte[] buffer = new byte[1024];
						int amountRead = -1;
						while(!isInterrupted() && ((amountRead = fromYTDL.read(buffer)) > -1))
						{
							logger.warn("youtube-dl error: " + new String(Arrays.copyOf(buffer, amountRead)));
						}
					}
					catch(IOException e)
					{
						logger.debug("youtube-dl", e);
					}
				}
			}.start();

			ExtendedTrack track = new ExtendedTrack(
					AudioSystem.getAudioInputStream(yProcess.getInputStream()),
					trackMeta);

			player.queue(track);
		}

		if(trackMetas.size() == 1)
		{
			sendPlayMessage(trackMetas.iterator().next(), channel);
		}
	}

	/**
	 * Sends a play message with the specified message builder.
	 *
	 * @param track
	 *      the {@link AudioPlayer.Track} that was queued
	 * @param channel
	 *      the {@link IChannel}
	 */
	private static void sendPlayMessage(TrackMeta track, IChannel channel)
	{
		try
		{
			new MessageBuilder(channel.getClient())
					.withChannel(channel)
					.appendContent("Queued ")
					.appendContent(track.toString(), MessageBuilder.Styles.BOLD)
					.build();
		}
		catch(DiscordException | RateLimitException | MissingPermissionsException e)
		{
			e.printStackTrace();
		}
	}



}