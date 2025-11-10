package com.github.langebangen.kensa.event.help;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class HelpEvent extends KensaEvent
{
    public HelpEvent(GatewayDiscordClient client, TextChannel textChannel)
    {
        super(client, textChannel);
    }
}
