package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class ShowPlaylistEvent extends KensaRadioEvent
{
	public ShowPlaylistEvent(IChannel channel, AudioPlayer player)
	{
		super(channel, player);
	}
}
