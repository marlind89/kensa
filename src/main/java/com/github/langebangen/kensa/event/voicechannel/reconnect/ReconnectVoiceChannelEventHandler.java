package com.github.langebangen.kensa.event.voicechannel.reconnect;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import org.reactivestreams.Publisher;

public class ReconnectVoiceChannelEventHandler implements EventHandler<ReconnectVoiceChannelEvent>
{
    private final VoiceConnections voiceConnections;

    @Inject
    public ReconnectVoiceChannelEventHandler(VoiceConnections voiceConnections)
    {
        this.voiceConnections = voiceConnections;
    }

    @Override
    public Publisher<?> handle(ReconnectVoiceChannelEvent event)
    {
        return event.getClient().getSelf()
            .flatMap(self -> self.asMember(event.getTextChannel().getGuildId()))
            .flatMap(Member::getVoiceState)
            .flatMap(VoiceState::getChannel)
            .flatMap(ac -> voiceConnections.reconnect(ac, true));
    }
}
