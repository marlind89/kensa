package com.github.langebangen.kensa.event.search;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import reactor.core.publisher.Flux;

public class SearchYoutubeEventHandler implements EventHandler<SearchYoutubeEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public SearchYoutubeEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<SearchYoutubeEvent> events)
    {
        return events
            .doOnNext(event -> musicPlayerManager.getMusicPlayer(event)
                .ifPresent(player -> player.searchYoutube(event)));
    }
}
