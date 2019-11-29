package com.github.langebangen.kensa.listener;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.VoiceChannel;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.listener.event.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LeaveVoiceChannelEvent;
import com.google.inject.Inject;

/**
 * @author Martin.
 */
public class VoiceChannelListener
	extends AbstractEventListener
{

	private final VoiceConnections voiceConnections;

	@Inject
	public VoiceChannelListener(DiscordClient client,
		VoiceConnections voiceConnections)
	{
		super(client);
		this.voiceConnections = voiceConnections;

		onChannelJoin();
		onChannelLeave();
	}


	private void onChannelJoin()
	{
		//TODO: If getVoiceChannelNameToJoin is null/empty then try to join
		// the voice channel where the user requesting the bot to join is currently.
		dispatcher.on(JoinVoiceChannelEvent.class)
			.flatMap(event -> event.getTextChannel().getGuild()
				.flatMap(guild -> guild.getChannels().ofType(VoiceChannel.class)
					.filter(channel -> channel.getName().equals(event.getVoiceChannelNameToJoin()))
					.singleOrEmpty()
					.flatMap(voiceConnections::join)
				)
				.doOnSuccess(vc ->
				{
					if (vc == null)
					{
						event.getTextChannel()
							.createMessage("No channel with name " + event.getVoiceChannelNameToJoin() + " exists!")
							.subscribe();
					}
				})
			)
			.subscribe();
	}

	private void onChannelLeave()
	{
		dispatcher.on(LeaveVoiceChannelEvent.class)
			.subscribe(event -> voiceConnections.disconnect(event.getTextChannel().getGuildId()));
	}
}
