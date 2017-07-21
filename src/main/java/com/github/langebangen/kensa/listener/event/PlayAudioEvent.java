package com.github.langebangen.kensa.listener.event;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * @author Martin.
 */
public class PlayAudioEvent extends KensaRadioEvent
{
	private final String url;
	private final boolean isPlaylistRequest;

	public PlayAudioEvent(IChannel textChannel, AudioPlayer player, String identifier,
		boolean isPlaylistRequest)
	{
		super(textChannel, player);
		this.url = identifier;
		this.isPlaylistRequest = isPlaylistRequest;
	}

	/**
	 * Gets the song identity requested
	 *
	 * @return
	 * 		the song identity requested
	 */
	public String getSongIdentity()
	{
		return url;
	}

	/**
	 * Returns whether this is a playlist request event
	 *
	 * @return
	 * 		whether this is a playlist request event
	 */
	public boolean isPlaylistRequest()
	{
		return isPlaylistRequest;
	}
}
