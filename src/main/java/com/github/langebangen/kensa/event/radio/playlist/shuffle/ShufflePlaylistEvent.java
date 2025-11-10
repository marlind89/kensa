package com.github.langebangen.kensa.event.radio.playlist.shuffle;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class ShufflePlaylistEvent extends KensaRadioEvent
{
    public ShufflePlaylistEvent(GatewayDiscordClient client,
        TextChannel textChannel)
    {
        super(client, textChannel);
    }
}
