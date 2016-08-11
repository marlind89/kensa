package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class KensaRadioEvent extends KensaEvent
{
	private final AudioPlayer player;

	public KensaRadioEvent(IChannel channel, AudioPlayer player)
	{
		super(channel);
		this.player = player;
	}

	public AudioPlayer getPlayer()
	{
		return player;
	}
}
