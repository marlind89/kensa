package com.github.langebangen.kensa.event.voicechannel.leave;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import reactor.core.publisher.Flux;

public class LeaveVoiceChannelEventHandler implements EventHandler<LeaveVoiceChannelEvent>
{
    private final VoiceConnections voiceConnections;

    @Inject
    public LeaveVoiceChannelEventHandler(VoiceConnections voiceConnections)
    {
        this.voiceConnections = voiceConnections;
    }

    @Override
    public Flux<?> handle(Flux<LeaveVoiceChannelEvent> events)
    {
        return events
            .flatMap(event -> voiceConnections.disconnect(event.getTextChannel().getGuildId()));
    }
}
