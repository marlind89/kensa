package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class LeaveVoiceChannelEvent extends KensaEvent
{
	public LeaveVoiceChannelEvent(IChannel channel)
	{
		super(channel);
	}
}
