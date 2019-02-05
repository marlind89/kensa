package com.github.langebangen.kensa.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

/**
 * Audio track for youtube which will get the best match
 * lazily on youtube right before it is processed.
 */
public class YoutubeBestMatchAudioTrack
	extends DelegatedAudioTrack
{

	private static final int SEARCH_RESULTS_LIMIT = 5;
	private final AudioTrackInfo initialAudioTrackInfo;
	private final YoutubeAudioSourceManager sourceManager;
	private final YoutubeSearchProvider ytSearchProvider;
	private YoutubeAudioTrack track;

	public YoutubeBestMatchAudioTrack(AudioTrackInfo initialAudioTrackInfo,
		YoutubeAudioSourceManager sourceManager,
		YoutubeSearchProvider ytSearchProvider)
	{
		super(initialAudioTrackInfo);
		this.initialAudioTrackInfo = initialAudioTrackInfo;
		this.sourceManager = sourceManager;
		this.ytSearchProvider = ytSearchProvider;
		this.track = null;
	}

	@Override
	public void process(LocalAudioTrackExecutor executor)
		throws Exception
	{
		if(track == null)
		{
			this.track = getTrack();
		}

		if(this.track != null)
		{
			this.processDelegate(track, executor);
		}
	}

	@Override
	public String getIdentifier()
	{
		return this.getInfo().identifier;
	}

	@Override
	public AudioTrackInfo getInfo()
	{
		if(track != null)
		{
			return track.getInfo();
		}

		return initialAudioTrackInfo;
	}

	@Override
	public boolean isSeekable()
	{
		if(track != null)
		{
			return track.isSeekable();
		}

		return false;
	}

	@Override
	public AudioTrack makeClone()
	{
		YoutubeBestMatchAudioTrack clone = new YoutubeBestMatchAudioTrack(this.getInfo(),
			sourceManager, ytSearchProvider);
		clone.setUserData(this.getUserData());

		return clone;
	}

	private YoutubeAudioTrack getTrack()
	{
		AudioTrackInfo trackInfo = this.getInfo();
		String query = trackInfo.title + " " + trackInfo.author;

		AudioItem audioItem = ytSearchProvider.loadSearchResult(query);

		if(audioItem == AudioReference.NO_TRACK)
		{
			return null;
		}
		else if(audioItem instanceof AudioPlaylist)
		{
			AudioPlaylist audioPlaylist = (AudioPlaylist) audioItem;

			// The number of matches is limited to reduce the chances of matching against
			// less than optimal results.
			// The best match is the one that has the smallest track duration delta.
			YoutubeAudioTrack bestMatch = audioPlaylist.getTracks().stream()
				.limit(SEARCH_RESULTS_LIMIT)
				.map(t -> (YoutubeAudioTrack)t)
				.min((o1, o2) -> {
					long o1TimeDelta = Math.abs(o1.getDuration() - trackInfo.length);
					long o2TimeDelta = Math.abs(o2.getDuration() - trackInfo.length);

					return (int)(o1TimeDelta - o2TimeDelta);
				}).orElse(null);

			return bestMatch;
		}
		else if(audioItem instanceof YoutubeAudioTrack)
		{
			return (YoutubeAudioTrack)audioItem;
		}
		else
		{
			return null;
		}
	}
}
