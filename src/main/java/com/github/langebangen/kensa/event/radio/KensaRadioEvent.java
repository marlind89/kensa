package com.github.langebangen.kensa.event.radio;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class KensaRadioEvent extends KensaEvent
{
    public KensaRadioEvent(GatewayDiscordClient client, TextChannel channel)
    {
        super(client, channel);
    }
}
