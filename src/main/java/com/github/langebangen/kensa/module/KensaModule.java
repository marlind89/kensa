package com.github.langebangen.kensa.module;

import com.github.langebangen.kensa.audio.lavaplayer.LavaplayerModule;
import com.github.langebangen.kensa.config.DatabaseConfig;
import com.github.langebangen.kensa.config.DiscordConfig;
import com.github.langebangen.kensa.config.SpotifyApiConfig;
import com.github.langebangen.kensa.config.YoutubeConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.retry.Retry;
import rita.RiMarkov;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
		bind(YoutubeConfig.class).toInstance(configProvider
			.bind("youtube", YoutubeConfig.class));
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
		return DiscordClientBuilder.create(discordConfig.token())
			// globally suppress any not found (404) error
			.onClientResponse(ResponseFunction.emptyIfNotFound())
			// bad requests (400) while adding reactions will be suppressed
			.onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
			// server error (500) while creating a message will be retried, with backoff, until it succeeds
			.onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
				Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
					.exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
			// wait 1 second and retry any server error (500)
			.onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500))
			.build();

	}

	@Provides
	@Singleton
	public GatewayDiscordClient provideGatewayDiscordClient(DiscordClient client){
		return client
				.gateway()
				.setEnabledIntents(IntentSet.all())
				.login()
				.block();
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