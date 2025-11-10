package com.github.langebangen.kensa.event.restart;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class RestartKensaEvent extends KensaEvent
{
    public RestartKensaEvent(GatewayDiscordClient client,
        TextChannel textChannel)
    {
        super(client, textChannel);
    }
}
