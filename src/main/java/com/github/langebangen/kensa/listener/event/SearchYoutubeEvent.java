package com.github.langebangen.kensa.listener.event;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * @author Martin.
 */
public class SearchYoutubeEvent
	extends KensaEvent
{
	private final String searchQuery;
	private final boolean isPlaylistSearch;

	public SearchYoutubeEvent(GatewayDiscordClient client, TextChannel textChannel,
		String searchQuery, boolean isPlaylistSearch)
	{
		super(client, textChannel);
		this.searchQuery = searchQuery;
		this.isPlaylistSearch = isPlaylistSearch;
	}

	public String getSearchQuery()
	{
		return searchQuery;
	}

	public boolean isPlaylistSearch()
	{
		return isPlaylistSearch;
	}
}
