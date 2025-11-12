package com.github.langebangen.kensa.event.search;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class SearchYoutubeEventHandler implements EventHandler<SearchYoutubeEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public SearchYoutubeEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(SearchYoutubeEvent event)
    {
        musicPlayerManager.getMusicPlayer(event).ifPresent(player -> player.searchYoutube(event));
        return Mono.empty();
    }
}
