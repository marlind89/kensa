package com.github.langebangen.kensa.event;

import discord4j.core.event.domain.Event;
import org.reactivestreams.Publisher;

public interface EventHandler<T extends Event>
    extends EventHandlerBase
{
    Publisher<?> handle(T event);
}
