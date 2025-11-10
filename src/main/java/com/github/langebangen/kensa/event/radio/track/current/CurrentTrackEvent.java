package com.github.langebangen.kensa.event.radio.track.current;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class CurrentTrackEvent extends KensaRadioEvent
{
    public CurrentTrackEvent(GatewayDiscordClient client, TextChannel textChannel)
    {
        super(client, textChannel);
    }
}
