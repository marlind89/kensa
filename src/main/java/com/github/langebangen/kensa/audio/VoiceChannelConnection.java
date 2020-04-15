package com.github.langebangen.kensa.audio;

import discord4j.core.object.entity.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;

public class VoiceChannelConnection
{
	private VoiceConnection voiceConnection;
	private VoiceChannel voiceChannel;
	private AudioProvider audioProvider;

	public VoiceChannelConnection(VoiceConnection voiceConnection, VoiceChannel voiceChannel, AudioProvider audioProvider)
	{
		this.voiceConnection = voiceConnection;
		this.voiceChannel = voiceChannel;
		this.audioProvider = audioProvider;
	}

	public VoiceChannel getVoiceChannel()
	{
		return voiceChannel;
	}
	public VoiceConnection getVoiceConnection()
	{
		return voiceConnection;
	}
	public AudioProvider getAudioProvider(){
		return audioProvider;
	}
}
