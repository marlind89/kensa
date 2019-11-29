package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class JoinVoiceChannelEvent extends KensaEvent
{
	private final String channelName;

	public JoinVoiceChannelEvent(DiscordClient client, TextChannel channel, String channelName)
	{
		super(client, channel);
		this.channelName = channelName;
	}

	public String getVoiceChannelNameToJoin()
	{
		return channelName;
	}
}
