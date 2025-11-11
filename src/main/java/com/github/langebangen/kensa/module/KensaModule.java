package com.github.langebangen.kensa.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.langebangen.kensa.audio.lavaplayer.LavaplayerModule;
import com.github.langebangen.kensa.config.*;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.apache.hc.core5.http.ParseException;
import org.cfg4j.provider.ConfigurationProvider;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.net.http.HttpClient;
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

    private final long voiceChannelId;
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
        bind(SentenceGeneratorConfig.class).toInstance(configProvider
            .bind("sentenceGenerator", SentenceGeneratorConfig.class));
        bind(SunoConfig.class).toInstance(configProvider
            .bind("suno", SunoConfig.class));

        var multibinder =
            Multibinder.newSetBinder(binder(), new TypeLiteral<EventHandler>()
            {
            });

        var reflections = new Reflections("com.github.langebangen.kensa.event");
        for (var impl : reflections.getSubTypesOf(EventHandler.class))
        {
            // Skip abstract classes
            if ((impl.getModifiers() & java.lang.reflect.Modifier.ABSTRACT) != 0)
            {
                continue;
            }

            multibinder.addBinding().to(impl);
        }
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
                        cc.getExpiresIn() - 60 * 2)), TimeUnit.SECONDS);
                    logger.info("Renewed Spotify access token");
                }
                catch (IOException | SpotifyWebApiException | ParseException e)
                {
                    logger.error("Failed to retrieve access token from Spotify", e);
                }
            }
        };
        getAccessTokenRunnable.run();

        return spotifyApi;
    }

    @Provides
    public HttpClient provideHttpClient()
    {
        return HttpClient.newHttpClient();
    }

    @Provides
    public ObjectMapper provideObjectMapper()
    {
        return new ObjectMapper();
    }
}