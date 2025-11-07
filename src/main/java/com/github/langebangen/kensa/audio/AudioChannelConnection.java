package com.github.langebangen.kensa.audio;

import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.voice.VoiceConnection;

public record AudioChannelConnection(
	VoiceConnection voiceConnection,
	AudioChannel audioChannel,
	MusicPlayer musicPlayer
) {};