package com.github.langebangen.kensa.event.insult;

import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.storage.Storage;
import com.github.langebangen.kensa.storage.generated.tables.records.InsultRecord;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

import static com.github.langebangen.kensa.storage.generated.Tables.INSULT;

public class SaveInsultEventHandler implements EventHandler<SaveInsultEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(SaveInsultEventHandler.class);

    private final Storage storage;

    @Inject
    public SaveInsultEventHandler(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public Flux<?> handle(Flux<SaveInsultEvent> events)
    {
        return events
            .flatMap(event ->
            {
                if (!event.isAdded() && LatestInsult.getLastInsultId() == -1)
                {
                    return event.getTextChannel()
                        .createMessage("No previous insult to remove!");
                }

                try (var conn = storage.getConnection())
                {
                    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
                    if (event.isAdded())
                    {
                        String insult = event.getInsult();
                        if (insult != null && !insult.isEmpty())
                        {
                            InsultRecord insultRecord = create.newRecord(INSULT);
                            insultRecord.setText(event.getInsult());
                            insultRecord.store();
                            return event.getTextChannel().createMessage("Insult added.");
                        }
                    }
                    else
                    {
                        create.delete(INSULT)
                            .where(INSULT.ID.equal(LatestInsult.getLastInsultId()))
                            .execute();
                        LatestInsult.setLastInsultId(-1);
                        return event.getTextChannel().createMessage("Removed previous insult.");
                    }
                }
                catch (SQLException e)
                {
                    logger.error("Error when persisting insult.", e);
                }
                return Mono.empty();
            });
    }
}
