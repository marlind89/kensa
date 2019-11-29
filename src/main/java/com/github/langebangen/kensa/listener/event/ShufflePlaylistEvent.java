package com.github.langebangen.kensa.listener.event;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

/**
 * @author Martin.
 */
public class ShufflePlaylistEvent extends KensaRadioEvent
{
	public ShufflePlaylistEvent(DiscordClient client,
		TextChannel textChannel)
	{
		super(client, textChannel);
	}
}
