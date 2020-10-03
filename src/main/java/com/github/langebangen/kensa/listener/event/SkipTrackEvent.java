package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
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
