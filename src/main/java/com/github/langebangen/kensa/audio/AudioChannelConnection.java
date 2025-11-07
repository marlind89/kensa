package com.github.langebangen.kensa.audio;

import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.voice.VoiceConnection;

public class AudioChannelConnection
{
	private final VoiceConnection voiceConnection;
	private final AudioChannel audioChannel;

	public AudioChannelConnection(VoiceConnection voiceConnection, AudioChannel audioChannel)
	{
		this.voiceConnection = voiceConnection;
		this.audioChannel = audioChannel;
	}

	public AudioChannel getAudioChannel()
	{
		return audioChannel;
	}
	public VoiceConnection getVoiceConnection()
	{
		return voiceConnection;
	}
}
