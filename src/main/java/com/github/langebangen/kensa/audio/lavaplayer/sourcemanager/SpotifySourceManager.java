package com.github.langebangen.kensa.audio.lavaplayer.sourcemanager;

import com.github.langebangen.kensa.audio.lavaplayer.audiotrack.YoutubeBestMatchAudioTrack;
import com.google.inject.Inject;
import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
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
				Paging<PlaylistTrack> playlistTracks = spotifyApi.getPlaylistsItems(playlistId)
						.market(CountryCode.SE)
						.limit(100)
						.offset(offset)
						.build()
						.execute();

				total = playlistTracks.getTotal();

				audioTracks.addAll(Arrays.stream(playlistTracks.getItems())
						.map(playlistTrack ->
						{
							var track = playlistTrack.getTrack();

							ArtistSimplified[] artists = track instanceof Track t
									? t.getArtists()
									: new ArtistSimplified[0];
							var firstArtistName = artists.length == 0 ? "" : artists[0].getName();

							return new YoutubeBestMatchAudioTrack(new AudioTrackInfo(firstArtistName + " - " + track.getName(), firstArtistName,
									track.getDurationMs(), "", false, ""),
									ytAudioSourceManager, ytSearchProvider, ytAudioSourceManager);
						})
						.collect(Collectors.toList()));

				offset += playlistTracks.getLimit();
			}

			return new BasicAudioPlaylist(playlist.getName() + " by " + playlist.getOwner().getDisplayName(), audioTracks, null, false);
		}
		catch(IOException | SpotifyWebApiException | ParseException e)
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
