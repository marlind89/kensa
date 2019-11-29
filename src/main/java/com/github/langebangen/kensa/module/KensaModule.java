package com.github.langebangen.kensa.module;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import rita.RiMarkov;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.lavaplayer.LavaplayerModule;
import com.github.langebangen.kensa.config.DatabaseConfig;
import com.github.langebangen.kensa.config.DiscordConfig;
import com.github.langebangen.kensa.config.SpotifyApiConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

/**
 * @author Martin.
 */
public class KensaModule
	extends AbstractModule
{
	private static final Logger logger = LoggerFactory.getLogger(KensaModule.class);

	private long voiceChannelId;
	private final ConfigurationProvider configProvider;

	public KensaModule(long voiceChannelId,
		ConfigurationProvider configProvider)
	{
		this.voiceChannelId = voiceChannelId;
		this.configProvider = configProvider;
	}

	@Override
	protected void configure()
	{
		install(new LavaplayerModule());

		bindConstant().annotatedWith(Names.named("latestVoiceChannelId")).to(voiceChannelId);
		bind(DatabaseConfig.class).toInstance(configProvider
			.bind("database", DatabaseConfig.class));
		bind(SpotifyApiConfig.class).toInstance(configProvider
			.bind("spotify", SpotifyApiConfig.class));
		bind(DiscordConfig.class).toInstance(configProvider
			.bind("discord", DiscordConfig.class));
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
	public DiscordClient getDiscordClient(DiscordConfig discordConfig)
	{
		return new DiscordClientBuilder(discordConfig.token()).build();
	}

	@Provides
	public EventDispatcher provideEventDispatcher(DiscordClient client){
		return client.getEventDispatcher();
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
}