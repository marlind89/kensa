package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.listener.event.BabylonEvent;
import com.github.langebangen.kensa.listener.event.HelpEvent;
import com.github.langebangen.kensa.listener.event.InsultEvent;
import com.github.langebangen.kensa.listener.event.InsultPersistEvent;
import com.github.langebangen.kensa.storage.Storage;
import com.github.langebangen.kensa.storage.generated.tables.Insult;
import com.github.langebangen.kensa.storage.generated.tables.records.InsultRecord;
import com.github.langebangen.kensa.util.KensaConstants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rita.RiMarkov;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.util.MessageBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.langebangen.kensa.storage.generated.Tables.INSULT;


/**
 * @author Martin.
 */
@Singleton
public class TextChannelListener
	extends AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(TextChannelListener.class);

	private final RiMarkov markov;
	private final Babylon babylon;
	private final Storage storage;

	private int lastInsultId;

	@Inject
	public TextChannelListener(IDiscordClient client,
		RiMarkov markov, Babylon babylon, Storage storage)
	{
		super(client);

		this.markov = markov;
		this.babylon = babylon;
		this.storage = storage;
		this.lastInsultId = -1;
	}

	@EventSubscriber
	public void onHelpEvent(HelpEvent event)
	{
		MessageBuilder messageBuilder = new MessageBuilder(client)
				.withChannel(event.getTextChannel());

		messageBuilder.appendContent("Kensa v" + KensaConstants.VERSION, MessageBuilder.Styles.ITALICS);
		messageBuilder.appendContent("\n");
		for(Action action : Action.values())
		{
			messageBuilder.appendContent("\n" + action.getAction(), MessageBuilder.Styles.BOLD);
			messageBuilder.appendContent(" - " + action.getDescription());
		}
		sendMessage(messageBuilder);
	}

	@EventSubscriber
	public void onBabylonEvent(BabylonEvent event)
	{
		sendMessage(event.getTextChannel(), "```" + babylon.getRandomDish() + "```");
	}

	@EventSubscriber
	public void onMentionEvent(MentionEvent event)
	{
		sendMessage(event.getMessage().getChannel(), markov.generateSentence());
	}

	@EventSubscriber
	public void onInsultEvent(InsultEvent event)
	{
		try(Connection conn = storage.getConnection())
		{
			DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);
			try(Stream<Record> stream = create.select()
					.from(INSULT)
					.orderBy(DSL.rand())
					.stream())
			{
				Optional<Record> first = stream.findFirst();
				if(first.isPresent())
				{
					Record record = first.get();
					String text = record.getValue(INSULT.TEXT);
					sendMessage(event.getTextChannel(), event.getUser().mention() + ", " + text);
					lastInsultId = record.getValue(INSULT.ID);
				}
			}
		}
		catch(SQLException e)
		{
			logger.error("Error when fetching insult from storage.", e);
		}
	}

	@EventSubscriber
	public void onInsultPersistEvent(InsultPersistEvent event)
	{
		if(event.isAdded() == false && lastInsultId == -1)
		{
			sendMessage(event.getTextChannel(), "No previous insult to remove!");
		}

		try(Connection conn = storage.getConnection())
		{
			DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);
			if(event.isAdded())
			{
				String insult = event.getInsult();
				if(insult != null && insult.isEmpty() == false)
				{
					InsultRecord insultRecord = create.newRecord(INSULT);
					insultRecord.setText(event.getInsult());
					insultRecord.store();
					sendMessage(event.getTextChannel(), "Insult added.");
				}
			}
			else
			{
				create.delete(INSULT)
					.where(INSULT.ID.equal(lastInsultId))
					.execute();
				sendMessage(event.getTextChannel(), "Removed previous insult.");
				lastInsultId = -1;
			}
		}
		catch(SQLException e)
		{
			logger.error("Error when persisting insult.", e);
		}
	}
}