package com.github.langebangen.kensa.event.babylon;

import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import reactor.core.publisher.Flux;

public class BabylonEventHandler implements EventHandler<BabylonEvent>
{
    private final Babylon babylon;

    @Inject
    public BabylonEventHandler(Babylon babylon)
    {
        this.babylon = babylon;
    }

    @Override
    public Flux<?> handle(Flux<BabylonEvent> events)
    {
        return events
            .flatMap(event -> event.getTextChannel()
                .createMessage("```" + babylon.getRandomDish() + "```"));
    }
}
