package com.github.langebangen.kensa.audio;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import com.github.langebangen.kensa.audio.lavaplayer.LavaPlayerAudioProvider;
import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

@Singleton
public class VoiceConnections
{

	private final AudioPlayerManager audioPlayerManager;
	private final MusicPlayerManager musicPlayerManager;
	private Map<Snowflake, VoiceChannelConnection> voiceConnections;

	@Inject
	private VoiceConnections(AudioPlayerManager audioPlayerManager,
		MusicPlayerManager musicPlayerManager){
		this.audioPlayerManager = audioPlayerManager;
		this.musicPlayerManager = musicPlayerManager;
		voiceConnections = new HashMap<>();
	}

	public Mono<VoiceConnection> join(VoiceChannel voiceChannel){

		AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
		AudioProvider audioProvider = new LavaPlayerAudioProvider(audioPlayer);
		musicPlayerManager.putMusicPlayer(voiceChannel.getGuildId(), audioPlayer);

		return voiceChannel.join(spec -> spec.setProvider(audioProvider))
			.doOnNext(voiceConnection -> addVoiceConnection(
				voiceChannel.getGuildId(),
				new VoiceChannelConnection(voiceConnection, voiceChannel)));
	}

	public VoiceChannel getVoiceChannel(Snowflake guildId){
		VoiceChannelConnection vcc = voiceConnections.get(guildId);
		if (vcc == null)
		{
			return null;
		}

		return vcc.getVoiceChannel();
	}

	public void disconnect(Snowflake guildId){
		VoiceChannelConnection removedVcc = voiceConnections.remove(guildId);
		if (removedVcc != null)
		{
			removedVcc.getVoiceConnection().disconnect();
		}
	}

	private void addVoiceConnection(Snowflake guildId, VoiceChannelConnection voiceConnection){
		voiceConnections.put(guildId, voiceConnection);
	}
}
