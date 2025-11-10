package com.github.langebangen.kensa.event.message;

import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;

import java.util.Random;

public class MimicHandler implements EventHandler<MessageCreateEvent>
{
    private final Random random;

    @Inject
    public MimicHandler()
    {
        this.random = new Random();
    }

    @Override
    public Flux<?> handle(Flux<MessageCreateEvent> events)
    {
        return events
            .map(MessageCreateEvent::getMessage)
            .filterWhen(event -> event.getAuthorAsMember().map(member -> !member.isBot()))
            .filter(message -> Command.parseCommand(message.getContent()) == null)
            .filter(message -> (random.nextFloat() * 1000) > 999)
            .flatMap(message -> message.getChannel()
                .flatMap(channel -> channel.createMessage("YEAH, " + message.getContent())));
    }
}
