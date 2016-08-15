package com.github.langebangen.kensa.audio;

import java.util.concurrent.TimeUnit;

/**
 * Class containing meta information for a track.
 */
public class TrackMeta
{
	private final TrackSource source;
	private final String url;
	private final String title;
	private final int durationInSeconds;
	private final String readableDuration;

	/**
	 * Constructor.
	 *
	 * @param source
	 * 		the {@link TrackSource}
	 * @param url
	 * 		the url string
	 * @param title
	 * 		the title
	 * @param durationInSeconds
	 * 		the duration in seconds, -1 if not specified
	 */
	public TrackMeta(TrackSource source, String url, String title, int durationInSeconds)
	{
		this.source = source;
		this.url = url;
		this.title = title;
		this.durationInSeconds = durationInSeconds;
		this.readableDuration = durationInSeconds < 0
				? null
				: String.format(" [%d min, %d sec]",
				TimeUnit.SECONDS.toMinutes(durationInSeconds),
				TimeUnit.SECONDS.toSeconds(durationInSeconds) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(durationInSeconds)));
	}

	@Override
	public String toString()
	{
		return String.format("%s %s", title != null ? title : url, readableDuration != null ? readableDuration : "");
	}
}