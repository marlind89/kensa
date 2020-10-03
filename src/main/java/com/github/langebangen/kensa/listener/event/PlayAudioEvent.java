package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class PlayAudioEvent extends KensaRadioEvent
{
	private final String url;
	private final boolean isPlaylistRequest;
	private final Member member;

	public PlayAudioEvent(GatewayDiscordClient client, TextChannel textChannel,
		String identifier, boolean isPlaylistRequest, Member member)
	{
		super(client, textChannel);
		this.url = identifier;
		this.isPlaylistRequest = isPlaylistRequest;
		this.member = member;
	}

	/**
	 * Gets the song identity requested
	 *
	 * @return
	 * 		the song identity requested
	 */
	public String getSongIdentity()
	{
		return url;
	}

	/**
	 * Returns whether this is a playlist request event
	 *
	 * @return
	 * 		whether this is a playlist request event
	 */
	public boolean isPlaylistRequest()
	{
		return isPlaylistRequest;
	}
	public Member getMember()
	{
		return member;
	}
}
