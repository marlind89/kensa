package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.event.EventStreamHandler;
import com.github.langebangen.kensa.event.radio.track.play.PlayTrackEvent;
import com.google.inject.Inject;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

public class FreskeJoinsVoiceChannelEventHandler implements EventStreamHandler<VoiceStateUpdateEvent>
{
    private final Random random;

    @Inject
    public FreskeJoinsVoiceChannelEventHandler()
    {
        this.random = new Random();
    }

    @Override
    public Flux<?> handle(Flux<VoiceStateUpdateEvent> events)
    {
        return events.publish(shared ->
            shared
                .filter(x -> x.getCurrent().getUserId().equals(x.getClient().getSelfId()) &&
                             x.getCurrent().getChannelId().isPresent())
                .switchMap(event -> shared
                    .filter(x ->
                    {
                        var current = x.getCurrent();
                        var old = x.getOld();
                        var channelId = event.getCurrent().getChannelId().get();
                        return current.getUserId().equals(Snowflake.of("144085745320198154")) &&
                               current.getChannelId()
                                   .map(chId -> chId.equals(channelId))
                                   .orElse(false) &&
                               old
                                   .flatMap(VoiceState::getChannelId)
                                   .map(oldChId -> !oldChId.equals(channelId))
                                   .orElse(true);
                    })
                    .flatMap(x -> Mono.justOrEmpty(x.getCurrent().getGuildId())
                        .zipWith(x.getCurrent().getMember()))
                    .map(t ->
                    {
                        var rand = random.nextInt(3);
                        String soundFile = switch (rand)
                        {
                            case 0 -> "fredrik.mp3";
                            case 1 -> "fredrik2.mp3";
                            case 2 -> "hjalp.mp3";
                            default -> throw new IllegalStateException("Unexpected value: " + rand);
                        };

                        return new PlayTrackEvent(event.getClient(), t.getT1(),
                            soundFile, false, t.getT2(), true);
                    })
                )
                .delayElements(Duration.ofMillis(500))
                .doOnNext(e -> e.getClient().getEventDispatcher().publish(e)));
    }
}
