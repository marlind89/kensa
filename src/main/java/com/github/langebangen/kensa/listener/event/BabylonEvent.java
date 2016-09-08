package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class BabylonEvent extends KensaEvent
{
	public BabylonEvent(IChannel textChannel)
	{
		super(textChannel);
	}
}
