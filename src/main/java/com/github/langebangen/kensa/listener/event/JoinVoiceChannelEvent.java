package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class JoinVoiceChannelEvent extends KensaEvent
{
	private final String channelName;

	public JoinVoiceChannelEvent(IChannel channel, String channelName)
	{
		super(channel);
		this.channelName = channelName;
	}

	public String getVoiceChannelNameToJoin()
	{
		return channelName;
	}
}
