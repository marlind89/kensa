package com.github.langebangen.kensa.audio.lavaplayer.sourcemanager;

import com.github.langebangen.kensa.audio.lavaplayer.audiotrack.SunoAudioTrack;
import com.github.langebangen.kensa.suno.SunoClient;
import com.github.langebangen.kensa.suno.models.SunoClip;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SunoSourceManager
    implements AudioSourceManager, HttpConfigurable
{
    private static final Logger logger = LoggerFactory.getLogger(SunoSourceManager.class);

    private final SunoClient sunoClient;
    private final HttpInterfaceManager httpInterfaceManager;

    @Inject
    public SunoSourceManager(SunoClient sunoClient)
    {
        this.sunoClient = sunoClient;
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    }

    @Override
    public String getSourceName()
    {
        return "Suno playlist";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference)
    {
        String identifier = reference.identifier;

        try
        {
            if (identifier.matches("https://suno.com/playlist/.+"))
            {
                return createPlaylist(identifier);
            }
            if (identifier.matches("https://suno.com/song/.+"))
            {
                return createSong(identifier);
            }
        }
        catch (IOException | InterruptedException e)
        {
            logger.error("Failed to load content from Suno", e);
        }

        return null;
    }

    private BasicAudioPlaylist createPlaylist(String identifier)
        throws IOException, InterruptedException
    {
        String playlistId = identifier.substring(identifier.lastIndexOf("/") + 1);

        var playlist = sunoClient.getPlaylist(playlistId);

        List<AudioTrack> audioTracks = playlist.playlist_clips().stream()
            .map(playlistTrack -> CreateTrack(playlistTrack.clip()))
            .collect(Collectors.toList());

        return new BasicAudioPlaylist(playlist.name() + " by " + playlist.user_display_name(),
            audioTracks, null, false);
    }

    private SunoAudioTrack createSong(String identifier)
        throws IOException, InterruptedException
    {
        var song = sunoClient.getSong(identifier);
        return CreateTrack(song);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track)
    {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output)
    {
        throw new UnsupportedOperationException("encodeTrack is unsupported.");
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input)
    {
        return new SunoAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown()
    {
        ExceptionTools.closeWithWarnings(httpInterfaceManager);
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator)
    {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator)
    {
        httpInterfaceManager.configureBuilder(configurator);
    }

    public HttpInterface getHttpInterface()
    {
        return httpInterfaceManager.getInterface();
    }

    private SunoAudioTrack CreateTrack(SunoClip clip)
    {
        var trackInfo = new AudioTrackInfo(clip.title(),
            clip.display_name(), (long) clip.metadata().duration() * 1000,
            clip.audio_url(), false, clip.audio_url());
        return new SunoAudioTrack(trackInfo, this);
    }
}
