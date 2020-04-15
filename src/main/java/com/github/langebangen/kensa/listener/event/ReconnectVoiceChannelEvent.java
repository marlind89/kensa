package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

public class ReconnectVoiceChannelEvent
	extends KensaEvent
{
	public ReconnectVoiceChannelEvent(DiscordClient client, TextChannel channel)
	{
		super(client, channel);
	}
}
