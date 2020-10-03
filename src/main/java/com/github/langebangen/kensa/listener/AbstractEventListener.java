package com.github.langebangen.kensa.listener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;

/**
 * @author Martin.
 */
public abstract class AbstractEventListener
{
	protected final EventDispatcher dispatcher;
	protected final GatewayDiscordClient client;

	protected AbstractEventListener(GatewayDiscordClient client)
	{
		this.client = client;
		this.dispatcher = client.getEventDispatcher();
	}
}