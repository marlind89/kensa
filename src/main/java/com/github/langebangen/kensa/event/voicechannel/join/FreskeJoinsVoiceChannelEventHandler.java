package com.github.langebangen.kensa.event.voicechannel.join;

import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.event.radio.track.play.PlayTrackEvent;
import com.google.inject.Inject;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

public class FreskeJoinsVoiceChannelEventHandler implements EventHandler<VoiceStateUpdateEvent>
{
    private final GatewayDiscordClient client;
    private final EventDispatcher dispatcher;
    private final Random random;

    @Inject
    public FreskeJoinsVoiceChannelEventHandler(GatewayDiscordClient client)
    {
        this.client = client;
        this.dispatcher = client.getEventDispatcher();
        this.random = new Random();
    }

    @Override
    public Flux<?> handle(Flux<VoiceStateUpdateEvent> events)
    {
        return events
            .filter(
                x -> x.getCurrent().getUserId().equals(client.getSelfId()) && x.getCurrent().getChannelId().isPresent())
            .switchMap(event ->
            {
                var currentVoiceChannelId = event.getCurrent().getChannelId().get();

                return events
                    .filter(x ->
                    {
                        var current = x.getCurrent();
                        var old = x.getOld();

                        return current.getUserId().equals(Snowflake.of("144085745320198154")) &&
                               current.getChannelId()
                                   .map(chId -> chId.equals(currentVoiceChannelId))
                                   .orElse(false) &&
                               old
                                   .flatMap(VoiceState::getChannelId)
                                   .map(oldChId -> !oldChId.equals(currentVoiceChannelId))
                                   .orElse(true);
                    })
                    .flatMap(x -> Mono.zip(
                        Mono.justOrEmpty(x.getCurrent().getGuildId()),
                        x.getCurrent().getMember()
                    ));
            })
            .delayElements(Duration.ofMillis(500))
            .doOnNext(tuple ->
            {
                var guildId = tuple.getT1();
                var member = tuple.getT2();

                var rand = random.nextInt(3);
                String soundFile = switch (rand)
                {
                    case 0 -> "fredrik.mp3";
                    case 1 -> "fredrik2.mp3";
                    case 2 -> "hjalp.mp3";
                    default -> throw new IllegalStateException("Unexpected value: " + rand);
                };

                dispatcher.publish(new PlayTrackEvent(client, guildId,
                    soundFile, false, member, true));
            });
    }
}
