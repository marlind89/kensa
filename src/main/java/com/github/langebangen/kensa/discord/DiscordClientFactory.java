package com.github.langebangen.kensa.discord;

import com.github.langebangen.kensa.config.DiscordConfig;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import io.netty.channel.unix.Errors;
import reactor.util.retry.Retry;

import java.time.Duration;

public class DiscordClientFactory
{

    public static DiscordClient createDiscordClient(DiscordConfig discordConfig)
    {
        return DiscordClientBuilder.create(discordConfig.token())
            // Suppress 404s globally
            .onClientResponse(ResponseFunction.emptyIfNotFound())

            // Suppress 400 Bad Request errors when adding reactions
            .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))

            // Global retry handler for all routes
            .onClientResponse(ResponseFunction.retryWhen(
                RouteMatcher.any(),
                Retry
                    .backoff(5, Duration.ofSeconds(2)) // retry up to 5 times, starting at 2s
                    .maxBackoff(Duration.ofSeconds(10)) // cap at 10s delay
                    .jitter(0.5) // ±50% randomization
                    .filter(throwable ->
                    {
                        // Retry on HTTP 5xx
                        if (throwable instanceof ClientException ce)
                        {
                            int code = ce.getStatus().code();
                            return code >= 500 && code < 600;
                        }
                        // Retry on transient network errors
                        return throwable instanceof Errors.NativeIoException;
                    })
            ))
            .build();

    }
}
