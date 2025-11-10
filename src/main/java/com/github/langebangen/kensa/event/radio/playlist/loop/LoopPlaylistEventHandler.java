package com.github.langebangen.kensa.event.radio.playlist.loop;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LoopPlaylistEventHandler implements EventHandler<LoopPlaylistEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public LoopPlaylistEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<LoopPlaylistEvent> events)
    {
        return events
            .flatMap(event ->
            {
                TextChannel channel = event.getTextChannel();

                String loopEnabled = event.getLoopEnabled() == null
                    ? ""
                    : event.getLoopEnabled();

                return musicPlayerManager.getMusicPlayer(event)
                    .map(player -> switch (loopEnabled)
                    {
                        case "on" ->
                        {
                            player.setLoopEnabled(true);
                            yield channel.createMessage("Looping enabled.");
                        }
                        case "off" ->
                        {
                            player.setLoopEnabled(false);
                            yield channel.createMessage("Looping disabled.");
                        }
                        default -> (Mono<Message>) channel.createMessage(
                            "Invalid loop command. Specify on or off, e.g. \"!loop on\"");
                    })
                    .orElse(Mono.empty());
            });
    }
}
