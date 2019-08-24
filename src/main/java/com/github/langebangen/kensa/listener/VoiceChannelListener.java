package com.github.langebangen.kensa.listener;

import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

import com.github.langebangen.kensa.listener.event.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.listener.event.LeaveVoiceChannelEvent;
import com.google.inject.Inject;

/**
 * @author Martin.
 */
public class VoiceChannelListener
	extends AbstractEventListener
{

	@Inject
	public VoiceChannelListener(IDiscordClient client)
	{
		super(client);
	}

	@EventSubscriber
	public void onChannelJoin(JoinVoiceChannelEvent event)
	{
		String voiceChannel = event.getVoiceChannelNameToJoin();
		IChannel channel = event.getTextChannel();
		List<IVoiceChannel> voiceChannels = channel.getGuild().getVoiceChannelsByName(voiceChannel);
		if(voiceChannels.isEmpty())
		{
			sendMessage(channel, "No channel with name " + voiceChannel + " exists!");
		}
		else
		{
			try
			{
				voiceChannels.get(0).join();
			}
			catch(MissingPermissionsException e)
			{
				sendMessage(channel, "I have no permission to join this channel :frowning2:");
			}
		}
	}

	@EventSubscriber
	public void onChannelLeave(LeaveVoiceChannelEvent event)
	{
		IGuild guild = event.getTextChannel().getGuild();
		client.getConnectedVoiceChannels()
				.stream()
				.filter(voiceChannel -> voiceChannel.getGuild().equals(guild))
				.forEach(IVoiceChannel::leave);
	}
}
