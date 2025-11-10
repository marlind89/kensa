package com.github.langebangen.kensa.audio.lavaplayer;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.event.KensaEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.github.langebangen.kensa.youtube.YoutubeApiService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating {@link MusicPlayer}s
 *
 * @author langen
 */
@Singleton
public class MusicPlayerManager
{
    private final Map<Snowflake, AudioMusicPlayer> musicPlayers;
    private final AudioPlayerManager playerManager;
    private final YoutubeApiService youtubeApiService;
    private final GatewayDiscordClient client;

    @Inject
    private MusicPlayerManager(GatewayDiscordClient client,
        AudioPlayerManager playerManager,
        YoutubeApiService youtubeApiService)
    {
        this.client = client;
        this.musicPlayers = new HashMap<>();
        this.playerManager = playerManager;
        this.youtubeApiService = youtubeApiService;
    }


    public AudioMusicPlayer getOrCreateMusicPlayer(Snowflake guildId)
    {
        return musicPlayers.computeIfAbsent(guildId, id ->
        {
            AudioPlayer audioPlayer = playerManager.createPlayer();
            audioPlayer.setVolume(50);
            TrackScheduler scheduler = new ClientTrackScheduler(audioPlayer);
            audioPlayer.addListener(scheduler);

            var musicPlayer = new LavaMusicPlayer(scheduler, playerManager, youtubeApiService);

            return new AudioMusicPlayer(audioPlayer, musicPlayer);
        });
    }

    public Optional<AudioMusicPlayer> getAudioMusicPlayer(Snowflake guildId)
    {
        return Optional.ofNullable(musicPlayers.get(guildId));
    }

    public Optional<MusicPlayer> getMusicPlayer(KensaEvent event)
    {
        return getMusicPlayer(event.getGuildId());
    }


    public Optional<MusicPlayer> getMusicPlayer(Snowflake guildId)
    {
        return getAudioMusicPlayer(guildId).map(AudioMusicPlayer::musicPlayer);
    }

    /**
     * A {@link TrackScheduler} which updates the "Now playing"
     * text for the Kensa bot.
     * <p>
     * Note that this class is not really suited for if Kensa
     * is connected to multiple guilds, since the "Now playing"
     * text is global.
     * <p>
     * Currently my use case is only for one Guild so I'm going
     * to use this for now since its a pretty sweet little function.
     */
    private class ClientTrackScheduler
        extends TrackScheduler
    {
        /**
         * @param player The audio player this scheduler uses
         */
        public ClientTrackScheduler(AudioPlayer player)
        {
            super(player);
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track)
        {
            super.onTrackStart(player, track);
            setTrackPlayingStatus(track);
        }

        @Override
        public void onPlayerPause(AudioPlayer player)
        {
            super.onPlayerPause(player);
            clearTrackPlayingStatus();
        }

        @Override
        public void onPlayerResume(AudioPlayer player)
        {
            super.onPlayerResume(player);

            var track = player.getPlayingTrack();

            if (track != null)
            {
                setTrackPlayingStatus(track);
            }
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
        {
            if (!hasNextTrack())
            {
                clearTrackPlayingStatus();
            }
            super.onTrackEnd(player, track, endReason);
        }

        private void clearTrackPlayingStatus()
        {
            client.updatePresence(ClientPresence.online()).subscribe();
        }

        private void setTrackPlayingStatus(AudioTrack track)
        {
            client.updatePresence(ClientPresence.online(ClientActivity.playing(TrackUtils.getReadableTrack(track))))
                .subscribe();
        }
    }
}
