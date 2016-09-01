package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;

/**
 * @author Martin.
 */
public class SearchYoutubeEvent
	extends KensaEvent
{
	private final String searchQuery;

	public SearchYoutubeEvent(IChannel textChannel, String searchQuery)
	{
		super(textChannel);
		this.searchQuery = searchQuery;
	}

	public String getSearchQuery()
	{
		return searchQuery;
	}
}
