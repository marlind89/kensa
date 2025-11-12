package com.github.langebangen.kensa.event.help;

import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.util.KensaConstants;
import discord4j.core.spec.EmbedCreateSpec;
import org.reactivestreams.Publisher;

public class HelpEventHandler implements EventHandler<HelpEvent>
{
    @Override
    public Publisher<?> handle(HelpEvent event)
    {
        var builder = EmbedCreateSpec.builder()
            .author("Kensa v" + KensaConstants.VERSION, "https://github.com/langebangen/kensa", null)
            .title("Available commands:");

        for (Action action : Action.values())
        {
            builder = builder.addField(action.getAction(), action.getDescription(), false);
        }

        return event.getTextChannel().createMessage(builder.build());
    }
}
