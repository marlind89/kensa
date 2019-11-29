package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class BabylonEvent extends KensaEvent
{
	public BabylonEvent(DiscordClient client, TextChannel textChannel)
	{
		super(client, textChannel);
	}
}
