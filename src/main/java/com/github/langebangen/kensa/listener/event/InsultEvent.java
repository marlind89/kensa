package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
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
