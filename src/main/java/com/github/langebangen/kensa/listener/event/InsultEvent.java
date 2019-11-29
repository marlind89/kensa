package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class InsultEvent extends KensaEvent
{
	private final Member member;

	public InsultEvent(DiscordClient client, TextChannel textChannel, Member member)
	{
		super(client, textChannel);
		this.member = member;
	}

	public Member getUser()
	{
		return member;
	}
}
