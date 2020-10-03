package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class LeaveVoiceChannelEvent extends KensaEvent
{
	public LeaveVoiceChannelEvent(GatewayDiscordClient client, TextChannel channel)
	{
		super(client, channel);
	}
}
