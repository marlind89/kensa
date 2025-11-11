package com.github.langebangen.kensa.event;

import com.github.langebangen.kensa.event.voicechannel.join.RejoinVoiceChannelEventHandler;
import com.google.inject.Inject;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class EventHandlerRegistrar
{
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerRegistrar.class);

    @SuppressWarnings("rawtypes")
    private final List<EventHandler> eventHandlers;

    @Inject
    @SuppressWarnings("rawtypes")
    public EventHandlerRegistrar(Set<EventHandler> eventHandlers)
    {
        // VERY important that RejoinVoiceChannelEventHandler is first,
        // because otherwise the ReadyEvent which it listens to isn't caught.
        this.eventHandlers = eventHandlers.stream()
            .sorted(java.util.Comparator.comparing(
                (EventHandler h) -> !(h instanceof RejoinVoiceChannelEventHandler)))
            .toList();
    }

    @SuppressWarnings("unchecked")
    public Mono<Void> register(EventDispatcher dispatcher)
    {
        var fluxes = eventHandlers.stream()
            .map(handler ->
            {
                Class<? extends Event> eventType = resolveEventType(handler.getClass());

                var flux = ((Flux<? extends Event>) handler.handle(dispatcher.on(eventType)))
                    .doOnError(ex -> logger.error("Error in {} handler", eventType.getSimpleName(), ex))
                    .retryWhen(
                        Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofMinutes(1))
                            .doBeforeRetry(rs ->
                                logger.warn("Resubscribing to {} stream (attempt {}): {}",
                                    eventType.getSimpleName(),
                                    rs.totalRetries() + 1,
                                    rs.failure().toString()))
                    );


                logger.info("Registered handler {} for event {}",
                    handler.getClass().getSimpleName(), eventType.getSimpleName());

                return flux;
            })
            .toList();

        return Mono.when(fluxes);
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends Event> resolveEventType(Class<?> handlerClass)
    {
        // Look at directly implemented interfaces
        for (var iface : handlerClass.getGenericInterfaces())
        {
            if (iface instanceof ParameterizedType pt && pt.getRawType() == EventHandler.class)
            {
                var arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> c && Event.class.isAssignableFrom(c))
                {
                    return (Class<? extends Event>) c;
                }
            }
        }
        // Walk superclasses if needed
        Class<?> current = handlerClass.getSuperclass();
        while (current != null)
        {
            for (var iface : current.getGenericInterfaces())
            {
                if (iface instanceof ParameterizedType pt && pt.getRawType() == EventHandler.class)
                {
                    var arg = pt.getActualTypeArguments()[0];
                    if (arg instanceof Class<?> c && Event.class.isAssignableFrom(c))
                    {
                        return (Class<? extends Event>) c;
                    }
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalArgumentException("Cannot resolve event type for handler: " + handlerClass.getName());
    }
}
