package com.github.langebangen.kensa.event.insult;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class SaveInsultEvent extends KensaEvent
{
    private final boolean added;
    private final String insult;

    public SaveInsultEvent(GatewayDiscordClient client, TextChannel textChannel, boolean added, String insult)
    {
        super(client, textChannel);
        this.added = added;
        this.insult = insult;
    }

    public boolean isAdded()
    {
        return added;
    }

    public String getInsult()
    {
        return insult;
    }
}
