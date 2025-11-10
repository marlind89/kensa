package com.github.langebangen.kensa.event.radio.track.skip;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class SkipTrackEvent extends KensaRadioEvent
{
    private final String skipAmount;

    public SkipTrackEvent(GatewayDiscordClient client, TextChannel textChannel, String skipAmount)
    {
        super(client, textChannel);
        this.skipAmount = skipAmount;
    }

    public String getSkipAmount()
    {
        return skipAmount;
    }

}
