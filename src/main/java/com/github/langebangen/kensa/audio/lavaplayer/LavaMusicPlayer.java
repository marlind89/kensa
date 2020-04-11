package com.github.langebangen.kensa.audio.lavaplayer;

import java.util.LinkedList;
import java.util.List;

import discord4j.core.object.entity.TextChannel;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.listener.event.PlayAudioEvent;
import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;

/**
 * Implementation of {@link MusicPlayer} using the lava player library
 *
 * @author langen
 */
public class LavaMusicPlayer
	implements MusicPlayer
{

	private final TrackScheduler trackScheduler;
	private final AudioPlayerManager playerManager;
	private final YoutubeSearchProvider ytSearchProvider;
	private final YoutubePlaylistSearchProvider ytPlaylistSearchProvider;
	private final SpotifyApi spotifyApi;
	private final YoutubeAudioSourceManager ytSourceManager;

	public LavaMusicPlayer(TrackScheduler trackScheduler, AudioPlayerManager playerManager,
		YoutubeSearchProvider ytSearchProvider,
		YoutubePlaylistSearchProvider ytPlaylistSearchProvider,
		SpotifyApi spotifyApi,
		YoutubeAudioSourceManager ytSourceManager)
	{
		this.trackScheduler = trackScheduler;
		this.playerManager = playerManager;
		this.ytSearchProvider = ytSearchProvider;
		this.ytPlaylistSearchProvider = ytPlaylistSearchProvider;
		this.spotifyApi = spotifyApi;
		this.ytSourceManager = ytSourceManager;
	}

	@Override
	public void stream(PlayAudioEvent event)
	{
		loadTrack(event.getSongIdentity(), event.getTextChannel(), event.isPlaylistRequest());
	}

	@Override
	public void searchYoutube(SearchYoutubeEvent event)
	{
		List<AudioTrackInfo> trackInfos = new LinkedList<>();
		if(event.isPlaylistSearch())
		{
			trackInfos.addAll(ytPlaylistSearchProvider.searchPlaylists(event.getSearchQuery()));
		}
		else
		{
			AudioItem audioItem = ytSearchProvider.loadSearchResult(event.getSearchQuery(),
				func -> new YoutubeAudioTrack(func, ytSourceManager));

			if(audioItem instanceof BasicAudioPlaylist)
			{
				for(AudioItem item : ((BasicAudioPlaylist)audioItem).getTracks())
				{
					if(item instanceof YoutubeAudioTrack)
					{
						YoutubeAudioTrack ytTrack = ((YoutubeAudioTrack)item);
						trackInfos.add(ytTrack.getInfo());
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder("```");
		if(trackInfos.isEmpty())
		{
			sb.append("Could not find anything matching the search query.");
		}
		else
		{
			for(AudioTrackInfo trackInfo : trackInfos)
			{
				String youtubeId = trackInfo.identifier;
				String title = trackInfo.title;
				// The length will be -1 if its a playlist since the playlist
				// length is not calculated
				String duration =
					trackInfo.length != -1 ? TrackUtils.getReadableDuration(trackInfo.length): "";
				sb.append(youtubeId);
				sb.append(" - " + title + " " + duration + "\n");
			}
		}
		sb.append("```");
		event.getTextChannel().createMessage(sb.toString()).subscribe();
	}

	@Override
	public void skipTrack()
	{
		trackScheduler.nextTrack();
	}

	@Override
	public void skipTrack(int skipAmount)
	{
		skipAmount = Math.max(0, skipAmount);
		for(int i = 0; i < skipAmount; i++)
		{
			skipTrack();
		}
	}

	@Override
	public AudioTrack getCurrentTrack()
	{
		return trackScheduler.getCurrentTrack();
	}

	@Override
	public List<AudioTrack> getPlayList()
	{
		return trackScheduler.getPlaylist();
	}

	@Override
	public void clearPlaylist()
	{
		trackScheduler.clear();
	}

	@Override
	public void pause(boolean pause)
	{
		trackScheduler.pause(pause);
	}

	@Override
	public boolean isPaused()
	{
		return trackScheduler.isPaused();
	}

	@Override
	public void setLoopEnabled(boolean loopEnabled)
	{
		trackScheduler.setLooping(loopEnabled);
	}

	@Override
	public void shuffle()
	{
		trackScheduler.shuffle();
	}

	private void loadTrack(String songIdentity, TextChannel channel, boolean isPlaylistRequest)
	{
		String playListIdentifier = null;
		if(isPlaylistRequest)
		{
			List<AudioTrackInfo> playListIdentifiers = ytPlaylistSearchProvider
				.searchPlaylists(songIdentity);
			playListIdentifier = playListIdentifiers.isEmpty() ?
				"ytsearch:" + songIdentity:
				playListIdentifiers.get(0).identifier;
		}

		final String identity = playListIdentifier != null ? playListIdentifier: songIdentity;

		playerManager.loadItemOrdered(trackScheduler, identity, new AudioLoadResultHandler()
		{

			private boolean fallbackSearchPerformed = false;

			@Override
			public void trackLoaded(AudioTrack track)
			{
				String readableTrack = TrackUtils.getReadableTrack(track);

				channel.createMessage("Queued **" + readableTrack + "**").subscribe();

				trackScheduler.queue(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist)
			{
				if(fallbackSearchPerformed)
				{
					// We will arrive here upon youtube search fallbacks,
					// but we only want to queue the first match in this case
					AudioTrack audioTrack = playlist.getTracks().get(0);
					loadTrack(audioTrack.getIdentifier(), channel, isPlaylistRequest);
				}
				else
				{
					channel.createMessage("Queued **" + playlist.getName()
						+ " [" + playlist.getTracks().size() + " songs]**")
						.subscribe();

					trackScheduler.queue(playlist);
				}
			}

			@Override
			public void noMatches()
			{
				if(!fallbackSearchPerformed && !identity.startsWith("http"))
				{
					playerManager.loadItemOrdered(trackScheduler, "ytsearch:" + identity, this);
					fallbackSearchPerformed = true;
				}
				else
				{
					channel.createMessage("Nope couldn't find that..").subscribe();
				}
			}

			@Override
			public void loadFailed(FriendlyException exception)
			{
				channel.createMessage(exception.getMessage()).subscribe();
			}
		});
	}

}