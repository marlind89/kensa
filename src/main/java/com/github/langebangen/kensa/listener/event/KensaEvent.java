package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class KensaEvent extends Event
{
	private final IChannel textChannel;

	public KensaEvent(IChannel textChannel)
	{
		this.textChannel = textChannel;
	}

	public IChannel getTextChannel()
	{
		return textChannel;
	}
}
