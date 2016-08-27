package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class PauseEvent extends KensaRadioEvent
{
	private final String shouldPause;

	/**
	 *
	 * @param channel
	 * @param player
	 * @param command
	 */
	public PauseEvent(IChannel channel, AudioPlayer player, String shouldPause)
	{
		super(channel, player);
		this.shouldPause = shouldPause;
	}

	public String shouldPause()
	{
		return shouldPause;
	}
}
