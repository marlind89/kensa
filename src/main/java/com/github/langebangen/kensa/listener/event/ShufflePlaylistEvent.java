package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class ShufflePlaylistEvent extends KensaRadioEvent
{
	public ShufflePlaylistEvent(IChannel textChannel, AudioPlayer player)
	{
		super(textChannel, player);
	}
}
