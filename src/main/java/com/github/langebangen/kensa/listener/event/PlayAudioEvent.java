package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

/**
 * @author Martin.
 */
public class PlayAudioEvent extends KensaRadioEvent
{
	private final String url;
	private final boolean isPlaylistRequest;

	public PlayAudioEvent(DiscordClient client, TextChannel textChannel,
		String identifier, boolean isPlaylistRequest)
	{
		super(client, textChannel);
		this.url = identifier;
		this.isPlaylistRequest = isPlaylistRequest;
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
}
