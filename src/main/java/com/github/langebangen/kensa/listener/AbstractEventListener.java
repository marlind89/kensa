package com.github.langebangen.kensa.listener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author Martin.
 */
public abstract class AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractEventListener.class);
	protected final EventDispatcher dispatcher;
	protected final GatewayDiscordClient client;

	protected AbstractEventListener(GatewayDiscordClient client)
	{
		this.client = client;
		this.dispatcher = client.getEventDispatcher();
	}
	
	protected <E extends Event, R> Disposable subscribe(Class<E> type, Function<Flux<E>, Flux<R>> chain) {
		return chain.apply(dispatcher.on(type))
			.doOnError(ex -> logger.error("Error in {} handler", type.getSimpleName(), ex))
			.retryWhen(
				Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
					.maxBackoff(Duration.ofMinutes(1))
					.doBeforeRetry(rs ->
						logger.warn("Resubscribing to {} stream (attempt {}): {}",
							type.getSimpleName(),
							rs.totalRetries() + 1,
							rs.failure().toString()))
			)
			.subscribe();
	}
	
	protected Disposable subscribe(Flux<?> flux) {
		return flux
			.doOnError(ex -> logger.error("Error in {} handler", ex.getClass().getSimpleName(), ex))
			.retryWhen(
				Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
					.maxBackoff(Duration.ofMinutes(1))
					.doBeforeRetry(rs ->
						logger.warn("Resubscribing (attempt {}): {}",
							rs.totalRetries() + 1,
							rs.failure().toString()))
			)
			.subscribe();
	}
}