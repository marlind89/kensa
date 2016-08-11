package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class LoopPlaylistEvent extends KensaRadioEvent
{
	private final String loopEnabled;
	public LoopPlaylistEvent(IChannel textChannel, AudioPlayer player, String loopEnabled)
	{
		super(textChannel, player);
		this.loopEnabled = loopEnabled;
	}

	public String getLoopEnabled()
	{
		return loopEnabled;
	}
}
