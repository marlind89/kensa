package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.listener.event.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LeaveVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.ReconnectVoiceChannelEvent;
import com.google.inject.Inject;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;

/**
 * @author Martin.
 */
public class VoiceChannelListener
	extends AbstractEventListener
{

	private final VoiceConnections voiceConnections;

	@Inject
	public VoiceChannelListener(GatewayDiscordClient client,
		VoiceConnections voiceConnections)
	{
		super(client);
		this.voiceConnections = voiceConnections;

		onChannelJoin();
		onChannelLeave();
		onChannelRejoin();
	}

	private void onChannelJoin()
	{
		dispatcher.on(JoinVoiceChannelEvent.class)
			.flatMap(event -> event.getTextChannel().getGuild()
				.flatMap(guild -> {
					Mono<VoiceChannel> vcToJoin;
					String voiceChannelNameToJoin = event.getVoiceChannelNameToJoin();
					if (voiceChannelNameToJoin == null || voiceChannelNameToJoin.isEmpty())
					{
						vcToJoin = event.getMember()
							.getVoiceState()
							.flatMap(VoiceState::getChannel);
					}
					else
					{
						vcToJoin = guild.getChannels().ofType(VoiceChannel.class)
							.filter(channel -> channel.getName().trim().equalsIgnoreCase(
								event.getVoiceChannelNameToJoin().trim()))
							.singleOrEmpty();
					}

					return vcToJoin.flatMap(voiceConnections::join);
				})
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

	private void onChannelRejoin()
	{
		dispatcher.on(ReconnectVoiceChannelEvent.class)
			.flatMap(event -> event.getClient().getSelf()
				.flatMap(self -> self.asMember(event.getTextChannel().getGuildId())))
			.flatMap(Member::getVoiceState)
			.flatMap(VoiceState::getChannel)
			.flatMap(vc -> voiceConnections.reconnect(vc, true))
			.subscribe();
	}
}
