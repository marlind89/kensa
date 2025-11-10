package com.github.langebangen.kensa.event.voicechannel.reconnect;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class ReconnectVoiceChannelEvent extends KensaEvent
{
    public ReconnectVoiceChannelEvent(GatewayDiscordClient client, TextChannel channel)
    {
        super(client, channel);
    }
}
