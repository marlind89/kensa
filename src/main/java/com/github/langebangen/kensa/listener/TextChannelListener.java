package com.github.langebangen.kensa.listener;

import static com.github.langebangen.kensa.storage.generated.Tables.INSULT;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import rita.RiMarkov;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.VoiceChannelConnection;
import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.listener.event.BabylonEvent;
import com.github.langebangen.kensa.listener.event.HelpEvent;
import com.github.langebangen.kensa.listener.event.InsultEvent;
import com.github.langebangen.kensa.listener.event.InsultPersistEvent;
import com.github.langebangen.kensa.listener.event.RestartKensaEvent;
import com.github.langebangen.kensa.storage.Storage;
import com.github.langebangen.kensa.storage.generated.tables.records.InsultRecord;
import com.github.langebangen.kensa.util.KensaConstants;
import com.google.inject.Inject;
import com.google.inject.Singleton;


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
	private final VoiceConnections voiceConnections;

	private int lastInsultId;

	@Inject
	public TextChannelListener(DiscordClient client,
		RiMarkov markov, Babylon babylon, Storage storage,
		VoiceConnections voiceConnections)
	{
		super(client);

		this.markov = markov;
		this.babylon = babylon;
		this.storage = storage;
		this.voiceConnections = voiceConnections;
		this.lastInsultId = -1;

		onHelpEvent();
		onBabylonEvent();
		onMentionEvent();
		onInsultEvent();
		onInsultPersistEvent();
		onRestartKensaEvent();
	}

	private void onHelpEvent()
	{
		dispatcher.on(HelpEvent.class)
			.flatMap(event -> event.getTextChannel().createEmbed(spec -> {
				spec.setAuthor("Kensa v" + KensaConstants.VERSION, "https://github.com/langebangen/kensa", null);
				spec.setTitle("Available commands:");

				for(Action action : Action.values())
				{
					spec.addField(action.getAction(), action.getDescription(), false);
				}
			}))
			.subscribe();
	}


	private void onBabylonEvent()
	{
		dispatcher.on(BabylonEvent.class)
			.flatMap(event -> event.getTextChannel()
				.createMessage("```" + babylon.getRandomDish() + "```"))
			.subscribe();
	}

	private void onMentionEvent()
	{
		dispatcher.on(MessageCreateEvent.class)
			.flatMap(event -> event.getGuild()
				.map(guild -> guild.getClient().getSelfId())
				.filter(Optional::isPresent)
				.filter(botId -> event.getMessage().getUserMentionIds().contains(botId.get()))
				.flatMap(botId -> event.getMessage().getChannel())
				.flatMap(channel -> channel.createMessage(markov.generateSentence()))
			)
			.subscribe(null, e -> logger.error("Failed on mention event", e));
	}

	private void onInsultEvent()
	{
		dispatcher.on(InsultEvent.class)
			.flatMap(event -> {
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
							lastInsultId = record.getValue(INSULT.ID);

							return event.getTextChannel().createMessage(
								event.getUser().getMention() + ", " + text);
						}
					}
				}
				catch(SQLException e)
				{
					logger.error("Error when fetching insult from storage.", e);
				}
				return Mono.empty();
			})
			.subscribe();
	}


	private void onInsultPersistEvent()
	{
		dispatcher.on(InsultPersistEvent.class)
			.flatMap(event -> {
				if(!event.isAdded() && lastInsultId == -1)
				{
					return event.getTextChannel()
						.createMessage("No previous insult to remove!");
				}

				try(Connection conn = storage.getConnection())
				{
					DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);
					if(event.isAdded())
					{
						String insult = event.getInsult();
						if(insult != null && !insult.isEmpty())
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
							.where(INSULT.ID.equal(lastInsultId))
							.execute();
						lastInsultId = -1;
						return event.getTextChannel().createMessage("Removed previous insult.");
					}
				}
				catch(SQLException e)
				{
					logger.error("Error when persisting insult.", e);
				}
				return Mono.empty();
			})
			.subscribe();
	}


	public void onRestartKensaEvent()
	{
		dispatcher.on(RestartKensaEvent.class)
			.flatMap(event ->
			{
				String voiceChannelId = "";

				VoiceChannelConnection vcc = voiceConnections
					.disconnect(event.getTextChannel().getGuildId());

				if (vcc != null)
				{
					voiceChannelId = " " + vcc.getVoiceChannel().getId().asLong();
				}

				return event.getTextChannel().createMessage("Restarting...")
					.then(event.getClient().logout())
					.thenReturn(voiceChannelId);
			})
			.doOnNext(voiceChannelId ->
			{
				List<String> command = new ArrayList<>();
				command.add("/bin/bash");
				command.add("-c");
				command.add("sleep 5 && ~/kensa/kensa.sh" + voiceChannelId);
				ProcessBuilder builder = new ProcessBuilder(command);

				try
				{
					builder.start();
					System.exit(0);
				}
				catch(IOException e)
				{
					logger.error("Failed to restart Kensa!" , e);
				}
			})
			.subscribe();
	}
}