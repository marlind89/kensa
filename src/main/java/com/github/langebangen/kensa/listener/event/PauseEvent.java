package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class PauseEvent extends KensaRadioEvent
{
	private final String shouldPause;

	public PauseEvent(GatewayDiscordClient client, TextChannel channel, String shouldPause)
	{
		super(client, channel);
		this.shouldPause = shouldPause;
	}

	public String shouldPause()
	{
		return shouldPause;
	}
}
