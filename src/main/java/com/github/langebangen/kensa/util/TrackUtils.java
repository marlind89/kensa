package com.github.langebangen.kensa.util;

import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

/**
 * Created by Martin on 2017-07-12.
 */
public class TrackUtils
{

	public static String getReadableTrack(AudioTrack audioTrack)
	{
		AudioTrackInfo trackInfo = audioTrack.getInfo();
		return String.format("%s %s", trackInfo.title != null
				? trackInfo.title : trackInfo.uri,
			getReadableDuration(audioTrack.getDuration()));
	}

	public static String getReadableDuration(long durationInMillis)
	{
		int durationInSeconds = Math.round(durationInMillis / 1000);
		return durationInMillis < 0
			? ""
			: String.format("[%dm, %ds]", TimeUnit.SECONDS.toMinutes(durationInSeconds),
			TimeUnit.SECONDS.toSeconds(durationInSeconds) - TimeUnit.MINUTES
				.toSeconds(TimeUnit.SECONDS.toMinutes(durationInSeconds)));

	}
}
