package com.github.langebangen.kensa.event.radio.playlist.clear;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class ClearPlaylistEvent extends KensaRadioEvent
{
    public ClearPlaylistEvent(GatewayDiscordClient client, TextChannel channel)
    {
        super(client, channel);
    }
}
