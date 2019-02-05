package com.github.langebangen.kensa.module;

import com.github.langebangen.kensa.audio.lavaplayer.sourcemanager.SpotifySourceManager;
import com.github.langebangen.kensa.config.DatabaseConfig;
import com.github.langebangen.kensa.config.SpotifyApiConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
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
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import rita.RiMarkov;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin.
 */
public class KensaModule
	extends AbstractModule
{
	private static final Logger logger = LoggerFactory.getLogger(KensaModule.class);

	private final String token;
	private long voiceChannelId;
	private final ConfigurationProvider configProvider;

	public KensaModule(String token, long voiceChannelId,
		ConfigurationProvider configProvider)
	{
		this.token = token;
		this.voiceChannelId = voiceChannelId;
		this.configProvider = configProvider;
	}

	@Override
	protected void configure()
	{
		bindConstant().annotatedWith(Names.named("latestVoiceChannelId")).to(voiceChannelId);
		bind(DatabaseConfig.class).toInstance(configProvider
			.bind("database", DatabaseConfig.class));
		bind(SpotifyApiConfig.class).toInstance(configProvider
			.bind("spotify", SpotifyApiConfig.class));
	}


	@Provides
	@Singleton
	public RiMarkov provideMarkov()
	{
		RiMarkov markov = new RiMarkov(3);
		if(new File("messages.txt").isFile())
		{
			markov.loadFrom("messages.txt");
		}
		return markov;
	}

	@Provides
	@Singleton
	public IDiscordClient getDiscordClient()
			throws DiscordException
	{
		return new ClientBuilder()
			.withToken(token)
			.build();
	}

	@Provides
	@Singleton
	public SpotifyApi getSpotifyApi(SpotifyApiConfig spotifyApiConfig)
	{
		SpotifyApi spotifyApi = new SpotifyApi.Builder()
			.setClientId(spotifyApiConfig.clientId())
			.setClientSecret(spotifyApiConfig.clientSecret())
			.build();

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		Runnable getAccessTokenRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ClientCredentialsRequest ccRequest = spotifyApi.clientCredentials().build();
					ClientCredentials cc = ccRequest.execute();
					spotifyApi.setAccessToken(cc.getAccessToken());
					// Renew the access token two minutes before it expires
					executorService.schedule(this, (Math.max(1,
						cc.getExpiresIn() - 60*2)), TimeUnit.SECONDS);
					logger.info("Renewed Spotify access token");
				}
				catch(IOException | SpotifyWebApiException e)
				{
					logger.error("Failed to retrieve access token from Spotify", e);
				}
			}
		};
		getAccessTokenRunnable.run();

		return spotifyApi;
	}


	@Provides
	@Singleton
	public YoutubeAudioSourceManager provideYoutubeAudioSourceManager()
	{
		return new YoutubeAudioSourceManager(true);
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
		return playerManager;
	}

	@Provides
	@Singleton
	public YoutubeSearchProvider provideYoutubeSearchProvider(
		YoutubeAudioSourceManager ytSourceManager)
	{
		return new YoutubeSearchProvider(ytSourceManager);
	}
}