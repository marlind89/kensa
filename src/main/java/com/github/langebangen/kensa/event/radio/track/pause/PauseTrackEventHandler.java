package com.github.langebangen.kensa.event.radio.track.pause;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class PauseTrackEventHandler implements EventHandler<PauseTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public PauseTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(PauseTrackEvent event)
    {
        return musicPlayerManager.getMusicPlayer(event)
            .map(player ->
            {
                String shouldPause = event.shouldPause() == null
                    ? ""
                    : event.shouldPause();

                switch (shouldPause)
                {
                    case "":
                        player.pause(!player.isPaused());
                        break;
                    case "on":
                        player.pause(true);
                        break;
                    case "off":
                        player.pause(false);
                        break;
                    default:
                        return event.getTextChannel()
                            .createMessage("Invalid pause command. Specify on or off, e.g. \"!pause on\"");
                }

                return Mono.empty();
            })
            .orElse(Mono.empty());
    }
}
