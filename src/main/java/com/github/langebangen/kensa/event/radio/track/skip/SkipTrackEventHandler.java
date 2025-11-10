package com.github.langebangen.kensa.event.radio.track.skip;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import reactor.core.publisher.Flux;

public class SkipTrackEventHandler implements EventHandler<SkipTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public SkipTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Flux<?> handle(Flux<SkipTrackEvent> events)
    {
        return events
            .doOnNext(event ->
            {
                String skipAmountString = event.getSkipAmount();
                musicPlayerManager.getMusicPlayer(event).ifPresent(player ->
                {
                    if (skipAmountString == null)
                    {
                        //Skip current song
                        player.skipTrack();
                    }
                    else if (!isInteger(skipAmountString))
                    {
                        event.getTextChannel().createMessage("That's not a valid number!")
                            .subscribe();
                    }
                    else
                    {
                        int skipAmount = Integer.parseInt(skipAmountString);
                        player.skipTrack(skipAmount);
                    }
                });
            });
    }

    private static boolean isInteger(String s)
    {
        return isInteger(s, 10);
    }

    private static boolean isInteger(String s, int radix)
    {
        if (s.isEmpty())
        {
            return false;
        }
        for (int i = 0;
             i < s.length();
             i++)
        {
            if (i == 0 && s.charAt(i) == '-')
            {
                if (s.length() == 1)
                {
                    return false;
                }
                else
                {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0)
            {
                return false;
            }
        }
        return true;
    }
}
