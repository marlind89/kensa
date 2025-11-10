package com.github.langebangen.kensa.event;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.Set;

public class EventHandlerRegistrar
{
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerRegistrar.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(EventDispatcher dispatcher, Injector injector)
    {
        var handlers = injector.getInstance(Key.get(new TypeLiteral<Set<EventHandler>>()
        {
        }));

        for (var handler : handlers)
        {
            Class<? extends Event> eventType = resolveEventType(handler.getClass());
            Flux<? extends Event> flux = dispatcher.on(eventType);

            ((Flux<?>) handler.handle((Flux) flux))
                .doOnError(ex -> logger.error("Error in {} handler", eventType.getSimpleName(), ex))
                .retryWhen(
                    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofMinutes(1))
                        .doBeforeRetry(rs ->
                            logger.warn("Resubscribing to {} stream (attempt {}): {}",
                                eventType.getSimpleName(),
                                rs.totalRetries() + 1,
                                rs.failure().toString()))
                )
                .subscribe();

            logger.info("Registered handler {} for event {}",
                handler.getClass().getSimpleName(), eventType.getSimpleName());
        }
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
