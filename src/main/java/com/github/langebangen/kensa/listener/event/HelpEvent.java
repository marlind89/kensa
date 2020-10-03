package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class HelpEvent extends KensaEvent
{
	public HelpEvent(GatewayDiscordClient client, TextChannel textChannel)
	{
		super(client, textChannel);
	}
}
