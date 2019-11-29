package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

public class RestartKensaEvent extends KensaEvent
{
	public RestartKensaEvent(DiscordClient client,
		TextChannel textChannel)
	{
		super(client, textChannel);
	}
}
