package com.github.langebangen.kensa.audio.lavaplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.listener.event.KensaEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.wrapper.spotify.SpotifyApi;

/**
 * Factory for creating {@link MusicPlayer}s
 *
 * @author langen
 */
@Singleton
public class MusicPlayerManager
{
	public final Map<Snowflake, MusicPlayer> musicPlayers;
	private final AudioPlayerManager playerManager;
	private final YoutubePlaylistSearchProvider ytPlaylistSearchProvider;
	private final YoutubeSearchProvider ytSearchProvider;
	private final DiscordClient client;
	private final SpotifyApi spotifyApi;

	@Inject
	private MusicPlayerManager(DiscordClient client, SpotifyApi spotifyApi,
		AudioPlayerManager playerManager)
	{
		this.client = client;
		this.spotifyApi = spotifyApi;
		this.musicPlayers = new HashMap<>();
		this.playerManager = playerManager;
		YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager(true);
		ytSearchProvider = new YoutubeSearchProvider(ytSourceManager);
		ytPlaylistSearchProvider = new YoutubePlaylistSearchProvider(ytSourceManager);
	}

	/**
	 * Gets the {@link MusicPlayer} associated with the specified {@link KensaEvent}.
	 * If no such {@link MusicPlayer} exists then it is created and the returned.
	 *
	 * @param event
	 * 		the {@link KensaEvent}
	 *
	 * @return
	 * 		the {@link MusicPlayer}
	 */
	public Optional<MusicPlayer> getMusicPlayer(KensaEvent event)
	{
		return getMusicPlayer(event.getTextChannel().getGuildId());
	}

	public void putMusicPlayer(Snowflake guildId, AudioPlayer audioPlayer){
		audioPlayer.setVolume(50);
		TrackScheduler scheduler = new ClientTrackScheduler(audioPlayer);
		audioPlayer.addListener(scheduler);

		musicPlayers.put(guildId, new LavaMusicPlayer(scheduler,
			playerManager, ytSearchProvider, ytPlaylistSearchProvider, spotifyApi));
	}

	/**
	 * Gets the {@link MusicPlayer} associated with the specified guild id.
	 * If no such {@link MusicPlayer} exists then it is created and the returned.
	 *
	 * @param guildId
	 * 		the guild id
	 *
	 * @return
	 * 		the {@link MusicPlayer}
	 */
	public Optional<MusicPlayer> getMusicPlayer(Snowflake guildId)
	{
		return Optional.ofNullable(musicPlayers.get(guildId));
	}

	/**
	 * A {@link TrackScheduler} which updates the "Now playing"
	 * text for the Kensa bot.
	 *
	 * Note that this class is not really suited for if Kensa
	 * is connected to multiple guilds, since the "Now playing"
	 * text is global.
	 *
	 * Currently my use case is only for one Guild so I'm going
	 * to use this for now since its a pretty sweet little function.
	 */
	private class ClientTrackScheduler
		extends TrackScheduler
	{
		/**
		 * @param player
		 * 	The audio player this scheduler uses
		 */
		public ClientTrackScheduler(AudioPlayer player)
		{
			super(player);
		}

		@Override
		public void onTrackStart(AudioPlayer player, AudioTrack track)
		{
			super.onTrackStart(player, track);

			client.updatePresence(Presence.online(Activity.playing(TrackUtils.getReadableTrack(track))))
				.subscribe();
		}

		@Override
		public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
		{
			if(!hasNextTrack())
			{
				client.updatePresence(Presence.online())
					.subscribe();
			}
			super.onTrackEnd(player, track, endReason);
		}
	}
}
