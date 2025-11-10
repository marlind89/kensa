package com.github.langebangen.kensa.event.insult;

import com.github.langebangen.kensa.event.KensaEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

public class InsultEvent extends KensaEvent
{
    private final Member member;

    public InsultEvent(GatewayDiscordClient client, TextChannel textChannel, Member member)
    {
        super(client, textChannel);
        this.member = member;
    }

    public Member getUser()
    {
        return member;
    }
}
