package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class ClearPlaylistEvent extends KensaRadioEvent
{
	public ClearPlaylistEvent(DiscordClient client, TextChannel channel)
	{
		super(client, channel);
	}
}
