package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

public class RestartKensaEvent extends KensaEvent
{
	public RestartKensaEvent(IChannel textChannel)
	{
		super(textChannel);
	}
}
