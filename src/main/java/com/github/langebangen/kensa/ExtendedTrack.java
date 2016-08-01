package com.github.langebangen.kensa;

import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * Extension of {@link Track} which contains additional
 * information about the track.
 *
 * @author langen
 */
public class ExtendedTrack
		extends Track
{
	private final TrackSource source;
	private final String url;
	private final String title;
	private final String duration;

	/**
	 * Constructor.
	 *
	 * @param provider
	 * 		the {@link IAudioProvider}
	 * @param source
	 * 		the {@link TrackSource}
	 * @param url
	 * 		the url string
	 * @param title
	 * 		the title
	 * @param duration
	 * 		the duration
	 */
	public ExtendedTrack(IAudioProvider provider, TrackSource source, String url,
	                     String title, String duration)
	{
		super(provider);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	/**
	 * Constructor.
	 *
	 * @param provider
	 * 		the {@link AudioInputStreamProvider}
	 * @param source
	 * 		the {@link TrackSource}
	 * @param url
	 * 		the url string
	 * @param title
	 * 		the title
	 * @param duration
	 * 		the duration
	 */
	public ExtendedTrack(AudioInputStreamProvider provider, TrackSource source, String url,
	                     String title, String duration)
			throws IOException
	{
		super(provider);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	/**
	 * Constructor.
	 *
	 * @param stream
	 * 		the {@link AudioInputStream}
	 * @param source
	 * 		the {@link TrackSource}
	 * @param url
	 * 		the url string
	 * @param title
	 * 		the title
	 * @param duration
	 * 		the duration
	 */
	public ExtendedTrack(AudioInputStream stream, TrackSource source, String url,
	                     String title, String duration)
			throws IOException
	{
		super(stream);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	/**
	 * Gets the {@link TrackSource}.
	 *
	 * @return trackSource
	 *      the {@link TrackSource}
	 */
	public TrackSource getTrackSource()
	{
		return source;
	}

	/**
	 * Gets the url string.
	 *
	 * @return url
	 *      the url string
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Gets the title.
	 *
	 * @return title
	 *      the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Gets the duration in a readable format string.
	 *
	 * @return duration
	 *      the duration
	 */
	public String getDuration()
	{
		return duration;
	}

	@Override
	public String toString()
	{
		return String.format("%s %s", title != null ? title : url, duration != null ? duration : "");
	}
}