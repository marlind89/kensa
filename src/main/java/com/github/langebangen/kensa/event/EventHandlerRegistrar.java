package com.github.langebangen.kensa.event;

import com.github.langebangen.kensa.event.voicechannel.join.RejoinVoiceChannelEventHandler;
import com.google.inject.Inject;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EventHandlerRegistrar
{
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerRegistrar.class);

    @SuppressWarnings("rawtypes")
    private final List<EventHandlerBase> eventStreamHandlers;

    @Inject
    @SuppressWarnings("rawtypes")
    public EventHandlerRegistrar(Set<EventHandlerBase> eventStreamHandlers)
    {
        // VERY important that RejoinVoiceChannelEventHandler is first,
        // because otherwise the ReadyEvent which it listens to isn't caught.
        this.eventStreamHandlers = eventStreamHandlers.stream()
            .sorted(java.util.Comparator.comparing(
                (EventHandlerBase h) -> !(h instanceof RejoinVoiceChannelEventHandler)))
            .toList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Mono<Void> register(EventDispatcher dispatcher)
    {
        var fluxes = eventStreamHandlers.stream()
            .map(handler ->
            {
                Class<? extends Event> eventType = resolveEventType(handler.getClass());
                Flux<?> flux = switch (handler)
                {
                    case EventHandler eventHandler -> dispatcher.on(eventType, e -> eventHandler.handle(e));
                    case EventStreamHandler streamHandler -> ((Flux<?>) streamHandler.handle(dispatcher.on(eventType)))
                        .onErrorResume(t ->
                        {
                            logger.error("Error while handling {}", eventType.getSimpleName(), t);
                            return Mono.empty();
                        });
                    default -> throw new IllegalStateException("Unknown handler type: " + handler.getClass().getName());
                };

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
        Class<?> current = handlerClass;
        while (current != null)
        {
            var resolved = Arrays.stream(current.getGenericInterfaces())
                .filter(i -> i instanceof ParameterizedType)
                .map(i -> (ParameterizedType) i)
                .filter(pt ->
                {
                    var raw = pt.getRawType();
                    return raw == EventStreamHandler.class || raw == EventHandler.class;
                })
                .map(pt -> pt.getActualTypeArguments()[0])
                .filter(arg -> arg instanceof Class<?> c && Event.class.isAssignableFrom(c))
                .map(arg -> (Class<? extends Event>) arg)
                .findFirst();

            if (resolved.isPresent())
            {
                return resolved.get();
            }

            current = current.getSuperclass();
        }
        throw new IllegalArgumentException("Cannot resolve event type for handler: " + handlerClass.getName());
    }
}
