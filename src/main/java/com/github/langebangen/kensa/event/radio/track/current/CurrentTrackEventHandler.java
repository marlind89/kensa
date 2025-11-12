package com.github.langebangen.kensa.event.radio.track.current;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.spec.EmbedCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class CurrentTrackEventHandler implements EventHandler<CurrentTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public CurrentTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(CurrentTrackEvent event)
    {
        var playerOpts = musicPlayerManager.getMusicPlayer(event);
        if (playerOpts.isEmpty())
        {
            return Mono.empty();
        }


        AudioTrack currentSong = playerOpts.get().getCurrentTrack();

        var embedSpec = EmbedCreateSpec.builder()
            .title("Current song: ")
            .description("**" + (currentSong != null ? TrackUtils.getReadableTrack(currentSong) : "none") + "**")
            .build();

        return event.getTextChannel().createMessage(embedSpec);
    }
}
