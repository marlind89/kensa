package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class InsultPersistEvent extends KensaEvent
{
	private final boolean added;
	private final String insult;

	public InsultPersistEvent(DiscordClient client, TextChannel textChannel, boolean added, String insult)
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
