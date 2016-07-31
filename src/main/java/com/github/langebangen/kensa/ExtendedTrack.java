package com.github.langebangen.kensa;

import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * @author langen
 */
public class ExtendedTrack
	extends AudioPlayer.Track
{
	private final TrackSource source;
	private final String url;
	private final String title;
	private final String duration;

	public ExtendedTrack(IAudioProvider provider, TrackSource source, String url, String title, String duration)
	{
		super(provider);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	public ExtendedTrack(AudioInputStreamProvider provider, TrackSource source, String url, String title, String duration)
		throws IOException
	{
		super(provider);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	public ExtendedTrack(AudioInputStream stream, TrackSource source, String url, String title, String duration)
		throws IOException
	{
		super(stream);
		this.source = source;
		this.url = url;
		this.title = title;
		this.duration = duration;
	}

	public TrackSource getSource()
	{
		return source;
	}

	public String getUrl()
	{
		return url;
	}

	public String getTitle()
	{
		return title;
	}

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
