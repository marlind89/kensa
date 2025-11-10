package com.github.langebangen.kensa.event.radio.playlist.shuffle;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ShufflePlaylistEventHandler implements EventHandler<ShufflePlaylistEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public ShufflePlaylistEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<ShufflePlaylistEvent> events)
    {
        return events
            .flatMap(event -> musicPlayerManager.getMusicPlayer(event)
                .map(player ->
                {
                    player.shuffle();
                    return (Mono<Message>) event.getTextChannel().createMessage("Playlist shuffled!");
                })
                .orElse(Mono.empty()));
    }
}
