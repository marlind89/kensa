package com.github.langebangen.kensa.event.radio.track.pause;

import com.github.langebangen.kensa.event.radio.KensaRadioEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class PauseTrackEvent extends KensaRadioEvent
{
    private final String shouldPause;

    public PauseTrackEvent(GatewayDiscordClient client, TextChannel channel, String shouldPause)
    {
        super(client, channel);
        this.shouldPause = shouldPause;
    }

    public String shouldPause()
    {
        return shouldPause;
    }
}
