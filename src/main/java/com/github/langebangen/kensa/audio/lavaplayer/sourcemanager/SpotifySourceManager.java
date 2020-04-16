package com.github.langebangen.kensa.audio.lavaplayer.sourcemanager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.langebangen.kensa.audio.lavaplayer.YoutubeBestMatchAudioTrack;
import com.google.inject.Inject;
import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;

public class SpotifySourceManager
	implements AudioSourceManager
{

	private final SpotifyApi spotifyApi;
	private final YoutubeAudioSourceManager ytAudioSourceManager;
	private final YoutubeSearchProvider ytSearchProvider;

	@Inject
	public SpotifySourceManager(SpotifyApi spotifyApi,
		YoutubeAudioSourceManager ytAudioSourceManager,
		YoutubeSearchProvider ytSearchProvider)
	{
		this.spotifyApi = spotifyApi;
		this.ytAudioSourceManager = ytAudioSourceManager;
		this.ytSearchProvider = ytSearchProvider;
	}

	@Override
	public String getSourceName()
	{
		return "Spotify playlist";
	}

	@Override
	public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference)
	{
		String identifier = reference.identifier;

		if (!identifier.matches("spotify:playlist:.+"))
		{
			return null;
		}

		String playlistId = identifier.substring(identifier.lastIndexOf("playlist:") + 9 );

		try
		{
			Playlist playlist = spotifyApi.getPlaylist(playlistId)
				.market(CountryCode.SE)
				.build()
				.execute();
			List<AudioTrack> audioTracks = new LinkedList<>();
			int total = Integer.MAX_VALUE;
			int offset = 0;
			while (total > offset)
			{
				Paging<PlaylistTrack> playlistTracks = spotifyApi.getPlaylistsTracks(playlistId)
					.market(CountryCode.SE)
					.limit(100)
					.offset(offset)
					.build()
					.execute();

				total = playlistTracks.getTotal();

				audioTracks.addAll(Arrays.stream(playlistTracks.getItems())
					.map(playlistTrack ->
					{
						Track track = playlistTrack.getTrack();

						ArtistSimplified[] artists = track.getArtists();
						String firstArtistName = artists.length == 0 ? "" : artists[0].getName();

						return new YoutubeBestMatchAudioTrack(new AudioTrackInfo(firstArtistName + " - " + track.getName(), firstArtistName,
							track.getDurationMs(), "", false, ""),
							ytAudioSourceManager, ytSearchProvider, ytAudioSourceManager);
					})
					.collect(Collectors.toList()));

				offset += playlistTracks.getLimit();
			}

			return new BasicAudioPlaylist(playlist.getName() + " by " + playlist.getOwner().getDisplayName(), audioTracks, null, false);
		}
		catch(IOException | SpotifyWebApiException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean isTrackEncodable(AudioTrack track)
	{
		return false;
	}

	@Override
	public void encodeTrack(AudioTrack track, DataOutput output)
	{
		throw new UnsupportedOperationException("encodeTrack is unsupported.");
	}

	@Override
	public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input)
	{
		throw new UnsupportedOperationException("decodeTrack is unsupported.");
	}

	@Override
	public void shutdown()
	{
	}
}
