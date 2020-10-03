package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class KensaRadioEvent extends KensaEvent
{


	public KensaRadioEvent(GatewayDiscordClient client, TextChannel channel)
	{
		super(client, channel);
	}
}
