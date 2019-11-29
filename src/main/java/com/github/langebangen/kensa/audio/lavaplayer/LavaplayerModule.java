package com.github.langebangen.kensa.audio.lavaplayer;

import com.github.langebangen.kensa.audio.lavaplayer.sourcemanager.SpotifySourceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

public class LavaplayerModule
	extends AbstractModule
{

	@Override
	protected void configure()
	{
		YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager(true);
		bind(YoutubeAudioSourceManager.class).toInstance(ytSourceManager);
		bind(YoutubeSearchProvider.class).toInstance(new YoutubeSearchProvider(ytSourceManager));
	}


	@Provides
	@Singleton
	public AudioPlayerManager provideAudioPlayerManager(
		YoutubeAudioSourceManager ytSourceManager,
		SpotifySourceManager spotifySourceManager)
	{
		DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		playerManager.registerSourceManager(ytSourceManager);
		playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
		playerManager.registerSourceManager(new BandcampAudioSourceManager());
		playerManager.registerSourceManager(new VimeoAudioSourceManager());
		playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		playerManager.registerSourceManager(new BeamAudioSourceManager());
		playerManager.registerSourceManager(new HttpAudioSourceManager());
		playerManager.registerSourceManager(spotifySourceManager);
		AudioSourceManagers.registerLocalSource(playerManager);
		playerManager.getConfiguration().setFrameBufferFactory((NonAllocatingAudioFrameBuffer::new));
		return playerManager;
	}
}
