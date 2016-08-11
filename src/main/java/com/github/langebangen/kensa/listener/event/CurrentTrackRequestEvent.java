package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class CurrentTrackRequestEvent extends KensaRadioEvent
{
	public CurrentTrackRequestEvent(IChannel textChannel, AudioPlayer player)
	{
		super(textChannel, player);
	}

	public AudioPlayer.Track getCurrentTrack()
	{
		return getPlayer().getCurrentTrack();
	}
}
