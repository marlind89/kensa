package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class KensaEvent extends Event
{
	private final TextChannel textChannel;

	public KensaEvent(DiscordClient client, TextChannel textChannel)
	{
		super(client);
		this.textChannel = textChannel;
	}

	public TextChannel getTextChannel()
	{
		return textChannel;
	}
}
