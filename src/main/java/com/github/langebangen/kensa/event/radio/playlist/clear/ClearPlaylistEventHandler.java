package com.github.langebangen.kensa.event.radio.playlist.clear;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ClearPlaylistEventHandler implements EventHandler<ClearPlaylistEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public ClearPlaylistEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<ClearPlaylistEvent> events)
    {
        return events
            .flatMap(event -> musicPlayerManager.getMusicPlayer(event)
                .map(player ->
                {
                    player.clearPlaylist();
                    return (Mono<Message>) event.getTextChannel().createMessage("Playlist cleared.");
                })
                .orElse(Mono.empty()));
    }
}
