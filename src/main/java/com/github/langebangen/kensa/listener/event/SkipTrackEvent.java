package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class SkipTrackEvent extends KensaRadioEvent
{
	private final String skipAmount;

	public SkipTrackEvent(DiscordClient client, TextChannel textChannel, String skipAmount)
	{
		super(client, textChannel);
		this.skipAmount = skipAmount;
	}

	public String getSkipAmount()
	{
		return skipAmount;
	}

}
