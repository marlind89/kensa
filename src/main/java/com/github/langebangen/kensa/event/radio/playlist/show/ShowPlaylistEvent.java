package com.github.langebangen.kensa.event.radio.playlist.show;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class ShowPlaylistEvent extends KensaRadioEvent
{
    public ShowPlaylistEvent(GatewayDiscordClient client, TextChannel channel)
    {
        super(client, channel);
    }
}
