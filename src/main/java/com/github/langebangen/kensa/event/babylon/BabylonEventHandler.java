package com.github.langebangen.kensa.event.babylon;

import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;

public class BabylonEventHandler implements EventHandler<BabylonEvent>
{
    private final Babylon babylon;

    @Inject
    public BabylonEventHandler(Babylon babylon)
    {
        this.babylon = babylon;
    }

    @Override
    public Publisher<?> handle(BabylonEvent event)
    {
        return event.getTextChannel().createMessage("```" + babylon.getRandomDish() + "```");
    }
}
