package com.github.langebangen.kensa.listener;

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;

/**
 * @author Martin.
 */
public abstract class AbstractEventListener
{
	protected final EventDispatcher dispatcher;
	protected final DiscordClient client;

	protected AbstractEventListener(DiscordClient client)
	{
		this.client = client;
		this.dispatcher = client.getEventDispatcher();
	}
}