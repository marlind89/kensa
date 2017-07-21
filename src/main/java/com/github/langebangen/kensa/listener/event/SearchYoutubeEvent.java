package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class SearchYoutubeEvent
	extends KensaEvent
{
	private final String searchQuery;
	private final boolean isPlaylistSearch;

	public SearchYoutubeEvent(IChannel textChannel, String searchQuery, boolean isPlaylistSearch)
	{
		super(textChannel);
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
