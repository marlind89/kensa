package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;


public class JoinVoiceChannelEvent extends KensaEvent
{
    private final String channelName;
    private final Member member;

    public JoinVoiceChannelEvent(GatewayDiscordClient client,
        TextChannel channel, String channelName, Member member)
    {
        super(client, channel);
        this.channelName = channelName;
        this.member = member;
    }

    public String getVoiceChannelNameToJoin()
    {
        return channelName;
    }

    public Member getMember()
    {
        return member;
    }
}
