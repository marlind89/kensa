package com.github.langebangen.kensa.event.radio.playlist.reaction;

import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.event.radio.playlist.show.ShowPlaylistEventHandler;
import com.google.inject.Inject;
import discord4j.core.event.domain.message.ReactionUserEmojiEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class PlaylistReactionEventHandler implements EventHandler<ReactionUserEmojiEvent>
{
    private final MusicPlayerManager musicPlayerManager;

    @Inject
    public PlaylistReactionEventHandler(MusicPlayerManager musicPlayerManager)
    {
        this.musicPlayerManager = musicPlayerManager;
    }

    @Override
    public Publisher<?> handle(ReactionUserEmojiEvent event)
    {
        return Mono.zip(event.getUser(), Mono.just(event.getEmoji()), Mono.just(event.getGuildId()))
            .filter(obj -> !obj.getT1().isBot())
            .doOnNext(tuple ->
            {
                var unicode = tuple.getT2().asUnicodeEmoji();
                var guildId = tuple.getT3();
                if (guildId.isPresent() && unicode.isPresent())
                {
                    musicPlayerManager
                        .getMusicPlayer(guildId.get())
                        .ifPresent(player ->
                        {
                            switch (unicode.get().getRaw())
                            {
                                case ShowPlaylistEventHandler.PLAY_PAUSE_EMOJI:
                                    player.pause(!player.isPaused());
                                    break;
                                case ShowPlaylistEventHandler.NEXT_TRACK_EMOJI:
                                    player.skipTrack();
                                    break;
                            }
                        });
                }
            });
    }
}
