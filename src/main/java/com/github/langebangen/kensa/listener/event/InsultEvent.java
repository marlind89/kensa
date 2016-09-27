package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author Martin.
 */
public class InsultEvent extends KensaEvent
{
	private final IUser user;

	public InsultEvent(IChannel textChannel, IUser user)
	{
		super(textChannel);
		this.user = user;
	}

	public IUser getUser()
	{
		return user;
	}
}
