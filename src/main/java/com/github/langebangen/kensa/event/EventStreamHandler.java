package com.github.langebangen.kensa.event;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;

public interface EventStreamHandler<T extends Event>
    extends EventHandlerBase
{
    Flux<?> handle(Flux<T> events);
}
