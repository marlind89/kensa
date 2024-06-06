package com.github.langebangen.kensa.sentence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.langebangen.kensa.config.SentenceGeneratorConfig;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class SentenceGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(SentenceGenerator.class);
    public static final String MESSAGES_FILE = "messages.txt";
    private final HttpClient httpClient;
    private final String url;
    private final ObjectMapper objectMapper;

    @Inject
    public SentenceGenerator(HttpClient httpClient,
        SentenceGeneratorConfig sentenceGeneratorConfig,
        ObjectMapper objectMapper)
    {
        this.httpClient = httpClient;
        this.url = sentenceGeneratorConfig.url();
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<String> generateSentence()
    {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(url + "/generate-sentence"))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(resp ->
            {
                var statusCode = resp.statusCode();
                var body = resp.body();
                if (statusCode / 100 != 2)
                {
                    throw new RuntimeException("Invalid response. Status: " + statusCode + ". Body: " + body);
                }

                try
                {
                    var jsonNode = objectMapper.readTree(body);
                    return jsonNode.get("sentence").asText();
                }
                catch (Exception e)
                {
                    logger.error("Failed parse sentence", e);
                    return "";
                }
            });
    }
}
