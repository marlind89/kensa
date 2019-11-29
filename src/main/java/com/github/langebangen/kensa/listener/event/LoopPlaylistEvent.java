package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class LoopPlaylistEvent extends KensaRadioEvent
{
	private final String loopEnabled;
	public LoopPlaylistEvent(DiscordClient client, TextChannel textChannel, String loopEnabled)
	{
		super(client, textChannel);
		this.loopEnabled = loopEnabled;
	}

	public String getLoopEnabled()
	{
		return loopEnabled;
	}
}
