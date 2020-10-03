package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

public class ReconnectVoiceChannelEvent
	extends KensaEvent
{
	public ReconnectVoiceChannelEvent(GatewayDiscordClient client, TextChannel channel)
	{
		super(client, channel);
	}
}
