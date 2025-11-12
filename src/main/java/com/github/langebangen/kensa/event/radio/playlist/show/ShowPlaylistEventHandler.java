package com.github.langebangen.kensa.event.radio.playlist.show;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ShowPlaylistEventHandler implements EventHandler<ShowPlaylistEvent>
{
    public static final String PLAY_PAUSE_EMOJI = "⏯";
    public static final String NEXT_TRACK_EMOJI = "⏭";

    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public ShowPlaylistEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(ShowPlaylistEvent event)
    {
        var playerOpts = musicPlayerManager.getMusicPlayer(event);
        if (playerOpts.isEmpty())
        {
            return Mono.empty();
        }

        var playlist = playerOpts.get().getPlayList();
        var channel = event.getTextChannel();
        if (playlist.isEmpty())
        {
            return channel.createMessage("No songs added to the playlist.");
        }
        else
        {
            var sb = new StringBuilder("```");

            int i = 1;
            var moreSongs = " \n and %d more...";
            for (AudioTrack track : playlist)
            {

                // The playlist size may be too large to send a message in
                // as the maximum message may be IMessage.MAX_MESSAGE_LENGTH characters long.
                // Reserving two digits for the amount of songs
                String trackString = String.format("\n %d. %s", i++, TrackUtils.getReadableTrack(track));
                if ((trackString.length() + sb.length()) <
                    (Message.MAX_CONTENT_LENGTH - moreSongs.length() - 2))
                {
                    sb.append(trackString);
                }
                else
                {
                    // We have reached the limit, print out the more songs string
                    sb.append(String.format(moreSongs, playlist.size() - i + 1));
                    break;
                }
            }

            String message = sb.toString();

            return channel.createMessage(
                    message.substring(0, Math.min(message.length(), Message.MAX_CONTENT_LENGTH - 4)) + "```")
                .flatMap(msg -> msg.addReaction(Emoji.unicode(PLAY_PAUSE_EMOJI))
                    .then(msg.addReaction(Emoji.unicode(NEXT_TRACK_EMOJI))));
        }
    }
}
