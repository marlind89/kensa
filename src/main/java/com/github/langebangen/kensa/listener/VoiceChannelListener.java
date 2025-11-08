package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.listener.event.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LeaveVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.ReconnectVoiceChannelEvent;
import com.google.inject.Inject;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.AudioChannel;
import reactor.core.publisher.Mono;

/**
 * @author Martin.
 */
public class VoiceChannelListener
    extends AbstractEventListener
{

    private final VoiceConnections voiceConnections;

    @Inject
    public VoiceChannelListener(GatewayDiscordClient client,
        VoiceConnections voiceConnections)
    {
        super(client);
        this.voiceConnections = voiceConnections;

        onChannelJoin();
        onChannelLeave();
        onChannelRejoin();
    }

    private void onChannelJoin()
    {
        subscribe(JoinVoiceChannelEvent.class, c -> c
            .flatMap(event -> event.getTextChannel().getGuild()
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
                })
            ));
    }

    private void onChannelLeave()
    {
        subscribe(LeaveVoiceChannelEvent.class, c -> c
            .flatMap(event -> voiceConnections.disconnect(event.getTextChannel().getGuildId())));
    }

    private void onChannelRejoin()
    {
        subscribe(ReconnectVoiceChannelEvent.class, c -> c
            .flatMap(event -> event.getClient().getSelf()
                .flatMap(self -> self.asMember(event.getTextChannel().getGuildId())))
            .flatMap(Member::getVoiceState)
            .flatMap(VoiceState::getChannel)
            .flatMap(ac -> voiceConnections.reconnect(ac, true)));
    }
}
