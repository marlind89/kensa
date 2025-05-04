package com.github.langebangen.kensa.audio.lavaplayer.audiotrack;

import com.github.langebangen.kensa.audio.lavaplayer.sourcemanager.SunoSourceManager;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Audio track that handles processing Suno tracks.
 */
public class SunoAudioTrack extends DelegatedAudioTrack
{
    private static final Logger log = LoggerFactory.getLogger(
        SunoAudioTrack.class);

    private final SunoSourceManager sourceManager;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public SunoAudioTrack(AudioTrackInfo trackInfo, SunoSourceManager sourceManager)
    {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor)
        throws Exception
    {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface())
        {
            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackInfo.uri), null))
            {
                processDelegate(new Mp3AudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    @Override
    protected AudioTrack makeShallowClone()
    {
        return new SunoAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager()
    {
        return sourceManager;
    }
}
