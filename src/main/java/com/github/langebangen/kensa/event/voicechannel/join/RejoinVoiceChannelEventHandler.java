package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class RejoinVoiceChannelEventHandler implements EventHandler<ReadyEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(RejoinVoiceChannelEventHandler.class);

    private final VoiceConnections voiceConnections;
    private final long latestVoiceChannelId;

    @Inject
    public RejoinVoiceChannelEventHandler(
        VoiceConnections voiceConnections,
        @Named("latestVoiceChannelId") long latestVoiceChannelId)
    {
        this.voiceConnections = voiceConnections;
        this.latestVoiceChannelId = latestVoiceChannelId;
    }

    @Override
    public Flux<?> handle(Flux<ReadyEvent> events)
    {
        return events
            .doOnNext(e -> logger.info("Logged in successfully!"))
            .filter(msg -> latestVoiceChannelId > 0)
            .flatMap(msg -> msg.getClient().getChannelById(Snowflake.of(latestVoiceChannelId)))
            .ofType(VoiceChannel.class)
            .flatMap(voiceChannel ->
            {
                logger.info("Rejoining channel {}", voiceChannel.getName());
                return voiceConnections.join(voiceChannel);
            });
    }
}
