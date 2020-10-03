package com.github.langebangen.kensa.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.VoiceChannel;
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

// TODO: Not sure if all reconnection logic is necessary anymore
// since upgrading d4j to 3.1.
// Another approach is to just disconnect Kensa from voice channels
// when nowhere is in the voice channel, and just reconnect
// when someone wants to listen to music again.

@Singleton
public class VoiceConnections
{
	private static final int MAX_RETRY_ATTEMPTS = 10;
	private static final int RETRY_INTERVAL_SECONDS = 1;

	private static final Logger logger = LoggerFactory.getLogger(VoiceConnections.class);

	private final AudioPlayerManager audioPlayerManager;
	private final MusicPlayerManager musicPlayerManager;
	private Map<Snowflake, VoiceChannelConnection> voiceConnections;

	@Inject
	private VoiceConnections(AudioPlayerManager audioPlayerManager,
		MusicPlayerManager musicPlayerManager){
		this.audioPlayerManager = audioPlayerManager;
		this.musicPlayerManager = musicPlayerManager;
		voiceConnections = new ConcurrentHashMap<>();
	}

	public synchronized Mono<VoiceConnection> join(VoiceChannel voiceChannel){
		AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
		musicPlayerManager.putMusicPlayer(voiceChannel.getGuildId(), audioPlayer);

		AudioProvider audioProvider = new LavaPlayerAudioProvider(audioPlayer);

		return joinVoiceChannel(audioProvider, voiceChannel);
	}

	public synchronized VoiceChannelConnection disconnect(Snowflake guildId)
	{
		VoiceChannelConnection vcc = RemoveVoiceChannelConnection(guildId);

		if (vcc == null)
		{
			return null;
		}

		logger.info("Found voice connection, disconnecting");

		vcc.getVoiceConnection().disconnect();

		return vcc;
	}

	public synchronized Mono<VoiceConnection> reconnect(VoiceChannel voiceChannel, boolean disconnectFirst)
	{
		logger.info("Reconnecting to voice channel: " + voiceChannel.getName());

		VoiceChannelConnection vcc = disconnectFirst
			? disconnect(voiceChannel.getGuildId())
			: GetVoiceChannelConnection(voiceChannel.getGuildId());

		if (vcc == null)
		{
			logger.warn("Didn't find any existing voice channel connection. Will create a new one.");
			return join(voiceChannel);
		}

		logger.info("Connecting again");

		return joinVoiceChannel(vcc.getAudioProvider(), voiceChannel);
	}

	private Mono<VoiceConnection> joinVoiceChannel(AudioProvider audioProvider, VoiceChannel voiceChannel)
	{
		return voiceChannel.join(spec -> spec.setProvider(audioProvider))
			.doOnNext(vc -> addVoiceConnection(voiceChannel.getGuildId(),
				new VoiceChannelConnection(vc, voiceChannel, audioProvider)));
	}

	private void addVoiceConnection(Snowflake guildId, VoiceChannelConnection voiceConnection)
	{
		logger.info("Adding new voice connection to voiceConnections map.");
		voiceConnections.put(guildId, voiceConnection);
	}

	private VoiceChannelConnection GetVoiceChannelConnection(Snowflake guildId)
	{
		return GetVoiceChannelConnection(guildId, () -> voiceConnections.get(guildId));
	}

	private VoiceChannelConnection RemoveVoiceChannelConnection(Snowflake guildId)
	{
		return GetVoiceChannelConnection(guildId, () -> voiceConnections.remove(guildId));
	}

	private VoiceChannelConnection GetVoiceChannelConnection(Snowflake guildId,
		Supplier<VoiceChannelConnection> getVoiceChannelConnectionFunction)
	{
		if (!musicPlayerManager.getMusicPlayer(guildId).isPresent()){
			return null;
		}

		int retryAttempts = 0;
		VoiceChannelConnection vcc;
		while((vcc = getVoiceChannelConnectionFunction.get()) == null)
		{
			retryAttempts++;

			logger.warn("Failed to find existing voice channel connection while trying to disconnect. "
				+ "Trying again in " + RETRY_INTERVAL_SECONDS + " seconds");

			try
			{
				Thread.sleep(RETRY_INTERVAL_SECONDS * 1000);
			}
			catch(InterruptedException e)
			{
				logger.error("Failed to sleep", e);
			}

			if (retryAttempts == MAX_RETRY_ATTEMPTS)
			{
				logger.warn("Giving up trying to find existing voice channel connection.");
				return null;

			}
		}

		return vcc;
	}
}
