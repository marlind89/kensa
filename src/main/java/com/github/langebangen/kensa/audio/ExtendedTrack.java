package com.github.langebangen.kensa.audio;

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
	private final TrackMeta trackMeta;

	/**
	 * Constructor.
	 *
	 * @param provider
	 * 		the {@link IAudioProvider}
	 * @param trackMeta
	 *      the {@link TrackMeta}
	 *
	 * @throws IOException
	 */
	public ExtendedTrack(IAudioProvider provider, TrackMeta trackMeta)
	{
		super(provider);
		this.trackMeta = trackMeta;
	}

	/**
	 * Constructor.
	 *
	 * @param provider
	 * 		the {@link AudioInputStreamProvider}
	 * @param trackMeta
	 *      the {@link TrackMeta}
	 *
	 * @throws IOException
	 */
	public ExtendedTrack(AudioInputStreamProvider provider, TrackMeta trackMeta)
			throws IOException
	{
		super(provider);
		this.trackMeta = trackMeta;
	}

	/**
	 * Constructor.
	 *
	 * @param stream
	 * 		the {@link AudioInputStream}
	 * @param trackMeta
	 *      the {@link TrackMeta}
	 *
	 * @throws IOException
	 */
	public ExtendedTrack(AudioInputStream stream, TrackMeta trackMeta)
			throws IOException
	{
		super(stream);
		this.trackMeta = trackMeta;
	}

	@Override
	public String toString()
	{
		return trackMeta.toString();
	}
}