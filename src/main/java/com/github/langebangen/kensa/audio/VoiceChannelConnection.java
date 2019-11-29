package com.github.langebangen.kensa.audio;

import discord4j.core.object.entity.VoiceChannel;
import discord4j.voice.VoiceConnection;

public class VoiceChannelConnection
{
	private VoiceConnection voiceConnection;
	private VoiceChannel voiceChannel;

	public VoiceChannelConnection(VoiceConnection voiceConnection, VoiceChannel voiceChannel)
	{
		this.voiceConnection = voiceConnection;
		this.voiceChannel = voiceChannel;
	}

	public VoiceChannel getVoiceChannel()
	{
		return voiceChannel;
	}
	public VoiceConnection getVoiceConnection()
	{
		return voiceConnection;
	}
}
