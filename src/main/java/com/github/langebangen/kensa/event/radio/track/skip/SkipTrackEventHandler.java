package com.github.langebangen.kensa.event.radio.track.skip;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class SkipTrackEventHandler implements EventHandler<SkipTrackEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public SkipTrackEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(SkipTrackEvent event)
    {
        String skipAmountString = event.getSkipAmount();
        return musicPlayerManager.getMusicPlayer(event).map(player ->
        {
            if (skipAmountString == null)
            {
                //Skip current song
                player.skipTrack();
                return Mono.empty();
            }

            if (!isInteger(skipAmountString))
            {
                return event.getTextChannel().createMessage("That's not a valid number!");
            }

            int skipAmount = Integer.parseInt(skipAmountString);
            player.skipTrack(skipAmount);
            return Mono.empty();
        }).orElse(Mono.empty());
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
