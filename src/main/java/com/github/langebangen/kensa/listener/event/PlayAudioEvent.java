package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class PlayAudioEvent extends KensaRadioEvent
{
	private final String url;

	public PlayAudioEvent(IChannel textChannel, AudioPlayer player, String url)
	{
		super(textChannel, player);
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}
}
