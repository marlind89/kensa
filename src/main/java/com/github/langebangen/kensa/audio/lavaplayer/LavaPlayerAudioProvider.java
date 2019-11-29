package com.github.langebangen.kensa.audio.lavaplayer;

import java.nio.ByteBuffer;

import discord4j.voice.AudioProvider;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an
 * IAudioProvider for D4J. As D4J calls canProvide before every call
 * to provide(), we pull the frame in canProvide() and use the frame
 * we already pulled in provide()
 *
 * @author langen
 */
public class LavaPlayerAudioProvider
	extends AudioProvider
{

	private final MutableAudioFrame frame = new MutableAudioFrame();
	private final AudioPlayer audioPlayer;

	/**
	 * @param audioPlayer
	 * 	Audio audioPlayer to wrap.
	 */
	public LavaPlayerAudioProvider(AudioPlayer audioPlayer)
	{
		// Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data for Discord
		super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
		// Set LavaPlayer's MutableAudioFrame to use the same buffer as the one we just allocated
		frame.setBuffer(getBuffer());
		this.audioPlayer = audioPlayer;
	}

	@Override
	public boolean provide() {
		// AudioPlayer writes audio data to its AudioFrame
		final boolean didProvide = audioPlayer.provide(frame);
		// If audio was provided, flip from write-mode to read-mode
		if (didProvide) {
			getBuffer().flip();
		}
		return didProvide;
	}
}