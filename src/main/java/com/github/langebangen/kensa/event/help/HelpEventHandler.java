package com.github.langebangen.kensa.event.help;

import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.util.KensaConstants;
import reactor.core.publisher.Flux;

public class HelpEventHandler implements EventHandler<HelpEvent>
{
    @Override
    public Flux<?> handle(Flux<HelpEvent> events)
    {
        return events
            .flatMap(event -> event.getTextChannel().createEmbed(spec ->
            {
                spec.setAuthor("Kensa v" + KensaConstants.VERSION, "https://github.com/langebangen/kensa", null);
                spec.setTitle("Available commands:");

                for (Action action : Action.values())
                {
                    spec.addField(action.getAction(), action.getDescription(), false);
                }
            }));
    }
}
