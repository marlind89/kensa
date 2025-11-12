package com.github.langebangen.kensa.event.insult;

import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.storage.Storage;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.sql.Connection;

import static com.github.langebangen.kensa.storage.generated.Tables.INSULT;

public class InsultEventHandler implements EventHandler<InsultEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(InsultEvent.class);
    private final Storage storage;

    @Inject
    public InsultEventHandler(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public Publisher<?> handle(InsultEvent event)
    {
        try (Connection conn = storage.getConnection())
        {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            try (var stream = create.select()
                .from(INSULT)
                .orderBy(DSL.rand())
                .stream())
            {
                var first = stream.findFirst();
                if (first.isPresent())
                {
                    var record = first.get();
                    String text = record.getValue(INSULT.TEXT);
                    LatestInsult.setLastInsultId(record.getValue(INSULT.ID));

                    return event.getTextChannel().createMessage(
                        event.getUser().getMention() + ", " + text);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when fetching insult from storage.", e);
            return Mono.error(e);
        }
        
        return Mono.empty();
    }
}
