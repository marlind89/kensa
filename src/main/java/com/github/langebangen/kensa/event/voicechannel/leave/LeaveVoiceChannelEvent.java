package com.github.langebangen.kensa.event.voicechannel.leave;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class LeaveVoiceChannelEvent extends KensaEvent
{
    public LeaveVoiceChannelEvent(GatewayDiscordClient client, TextChannel channel)
    {
        super(client, channel);
    }
}
