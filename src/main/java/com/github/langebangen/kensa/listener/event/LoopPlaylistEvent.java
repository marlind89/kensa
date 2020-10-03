package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class LoopPlaylistEvent extends KensaRadioEvent
{
	private final String loopEnabled;
	public LoopPlaylistEvent(GatewayDiscordClient client, TextChannel textChannel, String loopEnabled)
	{
		super(client, textChannel);
		this.loopEnabled = loopEnabled;
	}

	public String getLoopEnabled()
	{
		return loopEnabled;
	}
}
