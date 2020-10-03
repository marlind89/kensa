package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class JoinVoiceChannelEvent extends KensaEvent
{
	private final String channelName;
	private final Member member;

	public JoinVoiceChannelEvent(GatewayDiscordClient client,
		TextChannel channel, String channelName, Member member)
	{
		super(client, channel);
		this.channelName = channelName;
		this.member = member;
	}

	public String getVoiceChannelNameToJoin()
	{
		return channelName;
	}

	public Member getMember(){
		return member;
	}
}
