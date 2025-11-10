package com.github.langebangen.kensa.event;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;

public interface EventHandler<T extends Event>
{
    Flux<?> handle(Flux<T> events);
}
