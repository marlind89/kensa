package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class SkipTrackEvent extends KensaRadioEvent
{
	private final String skipAmount;

	public SkipTrackEvent(IChannel textChannel, AudioPlayer player, String skipAmount)
	{
		super(textChannel, player);
		this.skipAmount = skipAmount;
	}

	public String getSkipAmount()
	{
		return skipAmount;
	}

}
