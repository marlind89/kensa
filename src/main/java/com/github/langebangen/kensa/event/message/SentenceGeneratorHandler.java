package com.github.langebangen.kensa.event.message;

import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.sentence.SentenceGenerator;
import com.google.inject.Inject;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class SentenceGeneratorHandler implements EventHandler<MessageCreateEvent>
{
    private final SentenceGenerator sentenceGenerator;

    @Inject
    public SentenceGeneratorHandler(SentenceGenerator sentenceGenerator)
    {
        this.sentenceGenerator = sentenceGenerator;
    }

    @Override
    public Publisher<?> handle(MessageCreateEvent event)
    {
        return event.getGuild()
            .map(guild -> guild.getClient().getSelfId())
            .filter(botId -> event.getMessage().getUserMentionIds().contains(botId))
            .flatMap(botId -> event.getMessage().getChannel())
            .flatMap(channel -> Mono.fromFuture(sentenceGenerator.generateSentence().exceptionally(x -> ""))
                .flatMap(sentence -> sentence.isEmpty()
                    ? Mono.empty()
                    : channel.createMessage(sentence))
            );
    }
}
