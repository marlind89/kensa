package com.github.langebangen.kensa.audio.lavaplayer;

import com.github.langebangen.kensa.audio.lavaplayer.sourcemanager.SpotifySourceManager;
import com.github.langebangen.kensa.audio.lavaplayer.sourcemanager.SunoSourceManager;
import com.github.langebangen.kensa.config.YoutubeConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;

public class LavaplayerModule
    extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(YoutubeSearchProvider.class).toInstance(new YoutubeSearchProvider());
    }

    private static final Client[] DEFAULT_CLIENTS =
        new Client[]{new Music(), new AndroidVr(), new MWeb(), new WebEmbedded(), new Web(), new Android(),
            new AndroidMusic(), new Ios(), new Tv(), new TvHtml5Simply()};
    

    @Provides
    @Singleton
    public YoutubeAudioSourceManager provideYoutubeAudioSourceManager(YoutubeConfig config)
    {
        var opts = new YoutubeSourceOptions()
            .setRemoteCipher("https://cipher.kikkia.dev/", null, null)
            .setAllowSearch(true)
            .setAllowDirectVideoIds(true)
            .setAllowDirectPlaylistIds(true);
        var ytSourceManager = new YoutubeAudioSourceManager(opts, DEFAULT_CLIENTS);
        ytSourceManager.useOauth2(config.token(), false);

        return ytSourceManager;
    }


    @Provides
    @Singleton
    public AudioPlayerManager provideAudioPlayerManager(
        YoutubeAudioSourceManager ytSourceManager,
        SpotifySourceManager spotifySourceManager,
        SunoSourceManager sunoSourceManager)
    {
        DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(ytSourceManager);
//		playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(sunoSourceManager);
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(spotifySourceManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setFrameBufferFactory((NonAllocatingAudioFrameBuffer::new));
        return playerManager;
    }
}
