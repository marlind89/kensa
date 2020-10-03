package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.gateway.ShardInfo;

/**
 * @author Martin.
 */
public class KensaEvent extends Event
{
	private final TextChannel textChannel;

	public KensaEvent(GatewayDiscordClient client, TextChannel textChannel)
	{
		super(client, ShardInfo.create(0, 1));
		this.textChannel = textChannel;
	}

	public TextChannel getTextChannel()
	{
		return textChannel;
	}
}
