package com.github.langebangen.kensa.suno;

import com.github.langebangen.kensa.config.SunoConfig;
import com.github.langebangen.kensa.suno.models.SunoClip;
import com.github.langebangen.kensa.suno.models.SunoClipWrapper;
import com.github.langebangen.kensa.suno.models.SunoPlaylistSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

@Singleton
public class SunoClient
{
    private final HttpClient httpClient;
    private final Gson gson;
    private final SunoConfig sunoConfig;

    @Inject
    public SunoClient(SunoConfig sunoConfig)
    {
        this.sunoConfig = sunoConfig;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(SunoClipWrapper.class, new SunoClipWrapperDeserializer())
            .create();
    }

    public SunoPlaylistSchema getPlaylist(String playlistId)
        throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(sunoConfig.url() + "playlist/" + playlistId))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
        {
            throw new IOException("Failed to fetch data: HTTP " + response.statusCode());
        }

        return gson.fromJson(response.body(), SunoPlaylistSchema.class);
    }

    public SunoClip getSong(String songUrl)
        throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(songUrl))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        var json = parseFromHtml(response.body());

        return json != null
            ? gson.fromJson(json, SunoClipWrapper.class).clip()
            : null;
    }

    private String parseFromHtml(String html)
    {
        var doc = Jsoup.parse(html);
        for (var script : doc.select("script"))
        {
            String content = script.html();
            var matcher = Pattern.compile("5:(\\[.*\\]).*\\]", Pattern.DOTALL).matcher(content);
            if (matcher.find())
            {
                return matcher.group(1).replace("\\", "");
            }
        }
        return null;
    }
}