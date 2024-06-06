package com.github.langebangen.kensa.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.langebangen.kensa.config.SentenceGeneratorConfig;
import com.github.langebangen.kensa.sentence.ReloadSentenceModelPayload;
import com.github.langebangen.kensa.sentence.SentenceGenerator;
import com.github.langebangen.kensa.storage.Storage;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;

import static com.github.langebangen.kensa.storage.generated.Tables.MESSAGE;

public class UpdateMessagesOnDiskJob implements Job
{
    private static final Logger logger = LoggerFactory.getLogger(UpdateMessagesOnDiskJob.class);
    private final Storage storage;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String url;

    @Inject
    public UpdateMessagesOnDiskJob(Storage storage, HttpClient httpClient,
        ObjectMapper objectMapper, SentenceGeneratorConfig sentenceGeneratorConfig)
    {
        this.storage = storage;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.url = sentenceGeneratorConfig.url();
    }

    @Override
    public void execute(JobExecutionContext context)
    {
        GenerateMessageFile();
        ReloadSentenceModel();
    }

    private void GenerateMessageFile()
    {
        try (var conn = storage.getConnection())
        {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            var messageList = create
                .select(MESSAGE.TEXT)
                .from(MESSAGE)
                .fetch()
                .getValues(MESSAGE.TEXT)
                .stream()
                .flatMap(c -> Arrays.stream(c.split("\\.")))
                .filter(c -> !c.isEmpty())
                .map(c -> c.substring(0, 1).toUpperCase() + c.substring(1))
                .toList();

            var messages = String.join(". ", messageList)
                .replace("?.", "?")
                .replace("!.", "!");

            Files.writeString(Paths.get(SentenceGenerator.MESSAGES_FILE), messages);
        }
        catch (SQLException | IOException e)
        {
            logger.error("Failed to write messages to disk", e);
        }
    }

    private void ReloadSentenceModel()
    {
        try
        {
            var payload = new ReloadSentenceModelPayload();
            payload.setMessagesFilename(SentenceGenerator.MESSAGES_FILE);

            var jsonPayload = objectMapper.writeValueAsString(payload);

            var request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/reload-model"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e)
        {
            logger.error("Failed to initiate a reload of sentence model", e);
        }
    }
}
