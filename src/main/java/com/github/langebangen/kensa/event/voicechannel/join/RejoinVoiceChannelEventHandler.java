package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

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
    public Publisher<?> handle(ReadyEvent event)
    {
        logger.info("Logged in successfully!");
        if (latestVoiceChannelId <= 0)
        {
            return Mono.empty();
        }

        return event.getClient().getChannelById(Snowflake.of(latestVoiceChannelId))
            .ofType(VoiceChannel.class)
            .flatMap(voiceChannel ->
            {
                logger.info("Rejoining channel {}", voiceChannel.getName());
                return voiceConnections.join(voiceChannel);
            });
    }
}
