package com.github.langebangen.kensa.event.voicechannel.leave;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;

public class LeaveVoiceChannelEventHandler implements EventHandler<LeaveVoiceChannelEvent>
{
    private final VoiceConnections voiceConnections;

    @Inject
    public LeaveVoiceChannelEventHandler(VoiceConnections voiceConnections)
    {
        this.voiceConnections = voiceConnections;
    }

    @Override
    public Publisher<?> handle(LeaveVoiceChannelEvent event)
    {
        return voiceConnections.disconnect(event.getTextChannel().getGuildId());
    }
}
