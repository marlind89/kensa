package com.github.langebangen.kensa.audio;

import com.github.langebangen.kensa.audio.lavaplayer.LavaPlayerAudioProvider;
import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.core.spec.AudioChannelJoinSpec;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class VoiceConnections
{

    private static final Logger logger = LoggerFactory.getLogger(VoiceConnections.class);

    private final MusicPlayerManager musicPlayerManager;
    private final ConcurrentMap<Snowflake, AudioChannelConnection> activeConnections = new ConcurrentHashMap<>();

    @Inject
    public VoiceConnections(AudioPlayerManager audioPlayerManager,
        MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    /**
     * Join a voice channel reactively and start auto-leave monitoring.
     */
    public Mono<VoiceConnection> join(AudioChannel audioChannel)
    {
        logger.info("Joining voice channel: {}", audioChannel.getName());
        var guildId = audioChannel.getGuildId();
        var audioMusicPlayer = musicPlayerManager.getOrCreateMusicPlayer(audioChannel.getClient(), guildId);
        AudioProvider provider = new LavaPlayerAudioProvider(audioMusicPlayer.audioPlayer());

        return audioChannel.join(AudioChannelJoinSpec.builder()
                .provider(provider)
                .build())
            .flatMap(vc ->
            {
                var acc = new AudioChannelConnection(vc, audioChannel, audioMusicPlayer.musicPlayer());
                activeConnections.put(guildId, acc);
                acc.musicPlayer().pause(false);
                // Monitor channel for auto-leave
                return monitorAndAutoLeave(acc, audioChannel).thenReturn(vc);
            });
    }

    /**
     * Disconnect from the voice channel by guild ID reactively.
     * Emits the previous AudioChannelConnection (or completes empty if none).
     */
    public Mono<AudioChannelConnection> disconnect(Snowflake guildId)
    {
        AudioChannelConnection acc = activeConnections.remove(guildId);
        if (acc == null)
        {
            return Mono.empty();
        }
        // Convert existing value (already emitted) to Mono and perform disconnect
        return acc.voiceConnection().disconnect()
            .doOnSuccess(x ->
            {
                logger.info("Disconnected from voice channel: {}", acc.audioChannel().getName());
                acc.musicPlayer().pause(true);
            })
            .thenReturn(acc);
    }

    /**
     * Reconnect to a voice channel, optionally disconnecting first.
     */
    public Mono<VoiceConnection> reconnect(AudioChannel audioChannel, boolean disconnectFirst)
    {
        Mono<Void> pre = disconnectFirst
            ? disconnect(audioChannel.getGuildId())
            .delayElement(Duration.ofSeconds(5L)) // Wait a bit before rejoining
            .then()
            : Mono.empty();

        return pre.then(Mono.defer(() -> join(audioChannel)));
    }

    /**
     * Monitor the voice channel and disconnect when bot is alone.
     */
    private Mono<Void> monitorAndAutoLeave(AudioChannelConnection acc, AudioChannel channel)
    {
        // The bot itself has a VoiceState; 1 VoiceState signals bot is alone
        var voiceStateCounter = channel.getVoiceStates()
            .count()
            .map(count -> 1L == count);

        Mono<Void> onDelay = Mono.delay(Duration.ofSeconds(10L))
            .filterWhen(ignored -> voiceStateCounter)
            .switchIfEmpty(Mono.never())
            .then();

        var onEvent = channel.getClient().getEventDispatcher().on(VoiceStateUpdateEvent.class)
            .filter(
                event -> event.getOld().flatMap(VoiceState::getChannelId).map(channel.getId()::equals).orElse(false))
            .delaySequence(Duration.ofSeconds((10L)))
            .filterWhen(ignored -> voiceStateCounter)
            .next()
            .then();

        // Disconnect the bot if either onDelay or onEvent are completed!
        return Mono.firstWithSignal(onDelay, onEvent)
            .then(Mono.defer(() -> disconnect(channel.getGuildId())))
            .then();
    }
}