package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class PauseEvent extends KensaRadioEvent
{
	private final String shouldPause;

	public PauseEvent(DiscordClient client, TextChannel channel, String shouldPause)
	{
		super(client, channel);
		this.shouldPause = shouldPause;
	}

	public String shouldPause()
	{
		return shouldPause;
	}
}
