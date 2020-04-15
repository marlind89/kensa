package com.github.langebangen.kensa.audio;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.lavaplayer.LavaPlayerAudioProvider;
import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

@Singleton
public class VoiceConnections
{
	private static final Logger logger = LoggerFactory.getLogger(VoiceConnections.class);

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
			.doOnNext(voiceConnection -> addVoiceConnection(voiceChannel.getGuildId(),
				new VoiceChannelConnection(voiceConnection, voiceChannel, audioProvider)));
	}

	public VoiceChannel getVoiceChannel(Snowflake guildId){
		VoiceChannelConnection vcc = voiceConnections.get(guildId);
		if (vcc == null)
		{
			return null;
		}

		return vcc.getVoiceChannel();
	}

	public VoiceChannelConnection disconnect(Snowflake guildId){
		VoiceChannelConnection removedVcc = voiceConnections.remove(guildId);
		if (removedVcc != null)
		{
			removedVcc.getVoiceConnection().disconnect();
		}
		return removedVcc;
	}

	public Mono<VoiceConnection> reconnect(VoiceChannel voiceChannel){
		VoiceChannelConnection vcc = disconnect(voiceChannel.getGuildId());

		if (vcc == null)
		{
			return Mono.empty();
		}

		logger.info("Reconnecting to voice channel: " + voiceChannel.getName());

		AudioProvider audioProvider = vcc.getAudioProvider();

		return voiceChannel.join(spec -> spec.setProvider(audioProvider))
			.doOnNext(vc -> addVoiceConnection(voiceChannel.getGuildId(),
				new VoiceChannelConnection(vc, voiceChannel, audioProvider)));
	}

	private void addVoiceConnection(Snowflake guildId, VoiceChannelConnection voiceConnection){
		voiceConnections.put(guildId, voiceConnection);
	}
}
