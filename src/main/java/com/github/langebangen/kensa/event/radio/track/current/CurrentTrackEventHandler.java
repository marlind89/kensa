package com.github.langebangen.kensa.event.radio.track.current;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class CurrentTrackEventHandler implements EventHandler<CurrentTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public CurrentTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<CurrentTrackEvent> events)
    {
        return events
            .flatMap(event ->
            {
                Optional<MusicPlayer> playerOpts = musicPlayerManager.getMusicPlayer(event);
                if (playerOpts.isEmpty())
                {
                    return Mono.empty();
                }

                AudioTrack currentSong = playerOpts.get().getCurrentTrack();

                return event.getTextChannel().createEmbed(spec ->
                {
                    spec.setTitle("Current song: ");
                    spec.setDescription("**" + (currentSong != null
                                                    ? TrackUtils.getReadableTrack(currentSong)
                                                    : "none") + "**");
                });
            });
    }
}
