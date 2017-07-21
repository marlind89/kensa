package com.github.langebangen.kensa.audio;

import java.util.HashMap;
import java.util.Map;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

import com.github.langebangen.kensa.listener.event.KensaEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/**
 * Factory for creating {@link MusicPlayer}s
 *
 * @author langen
 */
@Singleton
public class MusicPlayerManager
{
	public final Map<Long, MusicPlayer> musicPlayers;
	private final AudioPlayerManager playerManager;
	private final YoutubePlaylistSearchProvider ytPlaylistSearchProvider;
	private final YoutubeSearchProvider ytSearchProvider;
	private final IDiscordClient client;

	@Inject
	private MusicPlayerManager(IDiscordClient client)
	{
		this.client = client;
		this.musicPlayers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager(true);
		ytSearchProvider = new YoutubeSearchProvider(ytSourceManager);
		ytPlaylistSearchProvider = new YoutubePlaylistSearchProvider(ytSourceManager);
		playerManager.registerSourceManager(ytSourceManager);
		playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
		playerManager.registerSourceManager(new BandcampAudioSourceManager());
		playerManager.registerSourceManager(new VimeoAudioSourceManager());
		playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		playerManager.registerSourceManager(new BeamAudioSourceManager());
		playerManager.registerSourceManager(new HttpAudioSourceManager());
		AudioSourceManagers.registerLocalSource(playerManager);
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
	public MusicPlayer getMusicPlayer(KensaEvent event)
	{
		return getMusicPlayer(event.getTextChannel().getGuild());
	}

	/**
	 * Gets the {@link MusicPlayer} associated with the specified {@link IGuild}.
	 * If no such {@link MusicPlayer} exists then it is created and the returned.
	 *
	 * @param guild
	 * 		the {@link IGuild}
	 *
	 * @return
	 * 		the {@link MusicPlayer}
	 */
	public MusicPlayer getMusicPlayer(IGuild guild)
	{
		long guildId = Long.parseLong(guild.getStringID());

		MusicPlayer musicPlayer = musicPlayers.get(guildId);
		if(musicPlayer == null)
		{
			AudioPlayer audioPlayer = playerManager.createPlayer();
			audioPlayer.setVolume(50);
			TrackScheduler scheduler = new ClientTrackScheduler(audioPlayer);
			audioPlayer.addListener(scheduler);
			musicPlayer = new LavaMusicPlayer(scheduler, playerManager, ytSearchProvider, ytPlaylistSearchProvider);
			guild.getAudioManager().setAudioProvider(new AudioProvider(audioPlayer));
			musicPlayers.put(guildId, musicPlayer);
		}

		return musicPlayer;
	};

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
		 * 	The audio audioPlayer this scheduler uses
		 */
		public ClientTrackScheduler(AudioPlayer player)
		{
			super(player);
		}

		@Override
		public void onTrackStart(AudioPlayer player, AudioTrack track)
		{
			super.onTrackStart(player, track);
			client.changePlayingText(TrackUtils.getReadableTrack(track));
		}

		@Override
		public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
		{
			if(!hasNextTrack())
			{
				client.changePlayingText(null);
			}
			super.onTrackEnd(player, track, endReason);
		}
	}
}
