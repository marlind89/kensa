package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.AudioChannel;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class JoinVoiceChannelEventHandler implements EventHandler<JoinVoiceChannelEvent>
{
    private final VoiceConnections voiceConnections;

    @Inject
    public JoinVoiceChannelEventHandler(VoiceConnections voiceConnections)
    {
        this.voiceConnections = voiceConnections;
    }

    @Override
    public Publisher<?> handle(JoinVoiceChannelEvent event)
    {
        return event.getTextChannel().getGuild()
            .flatMap(guild ->
            {
                Mono<AudioChannel> vcToJoin;
                String voiceChannelNameToJoin = event.getVoiceChannelNameToJoin();
                if (voiceChannelNameToJoin == null || voiceChannelNameToJoin.isEmpty())
                {
                    vcToJoin = event.getMember()
                        .getVoiceState()
                        .flatMap(VoiceState::getChannel);
                }
                else
                {
                    vcToJoin = guild.getChannels().ofType(AudioChannel.class)
                        .filter(channel -> channel.getName().trim().equalsIgnoreCase(
                            event.getVoiceChannelNameToJoin().trim()))
                        .singleOrEmpty();
                }

                return vcToJoin.flatMap(voiceConnections::join);
            })
            .doOnSuccess(vc ->
            {
                if (vc == null)
                {
                    event.getTextChannel()
                        .createMessage("No channel with name " + event.getVoiceChannelNameToJoin() + " exists!")
                        .subscribe();
                }
            });
    }
}
