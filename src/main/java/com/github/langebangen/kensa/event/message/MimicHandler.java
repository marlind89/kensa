package com.github.langebangen.kensa.event.message;

import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

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
    public Publisher<?> handle(MessageCreateEvent event)
    {
        var message = event.getMessage();
        if (Command.parseCommand(message.getContent()) != null || (random.nextFloat() * 1000) < 999)
        {
            return Mono.empty();
        }
        
        return message.getAuthorAsMember()
            .filter(member -> !member.isBot())
            .flatMap(member -> message.getChannel())
            .flatMap(channel -> channel.createMessage("YEAH, " + message.getContent()));
    }
}
