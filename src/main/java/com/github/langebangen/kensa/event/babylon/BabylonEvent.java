package com.github.langebangen.kensa.event.babylon;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class BabylonEvent extends KensaEvent
{
    public BabylonEvent(GatewayDiscordClient client, TextChannel textChannel)
    {
        super(client, textChannel);
    }
}
