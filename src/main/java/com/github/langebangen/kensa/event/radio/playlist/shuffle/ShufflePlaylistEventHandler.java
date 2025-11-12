package com.github.langebangen.kensa.event.radio.playlist.shuffle;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.entity.Message;
import org.reactivestreams.Publisher;
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
    public Publisher<?> handle(ShufflePlaylistEvent event)
    {
        return musicPlayerManager.getMusicPlayer(event)
            .map(player ->
            {
                player.shuffle();
                return (Mono<Message>) event.getTextChannel().createMessage("Playlist shuffled!");
            })
            .orElse(Mono.empty());
    }
}
