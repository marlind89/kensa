package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class InsultPersistEvent extends KensaEvent
{
	private final boolean added;
	private final String insult;

	public InsultPersistEvent(IChannel textChannel, boolean added, String insult)
	{
		super(textChannel);
		this.added = added;
		this.insult = insult;
	}

	public boolean isAdded()
	{
		return added;
	}

	public String getInsult()
	{
		return insult;
	}
}
