package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class HelpEvent extends KensaEvent
{
	public HelpEvent(IChannel textChannel)
	{
		super(textChannel);
	}
}
