package com.github.langebangen.kensa.event.radio.track.play;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class PlayTrackEventHandler implements EventHandler<PlayTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public PlayTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(PlayTrackEvent event)
    {
        musicPlayerManager.getMusicPlayer(event).ifPresent(player -> player.stream(event));
        return Mono.empty();
    }
}
