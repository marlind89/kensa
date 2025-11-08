package com.github.langebangen.kensa.youtube;

import com.github.langebangen.kensa.config.YoutubeConfig;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YoutubeApiService
{
    private static final Logger logger = LoggerFactory.getLogger(YoutubeApiService.class);

    private static final String APPLICATION_NAME = "Kensa";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final YouTube apiService;
    private final YoutubeConfig config;

    @Inject
    public YoutubeApiService(YoutubeConfig config)
        throws IOException, GeneralSecurityException
    {
        this.config = config;

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        apiService = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public List<AudioTrackInfo> search(String query)
    {
        try
        {
            YouTube.Search.List request = apiService.search()
                .list(List.of("snippet"));

            String apiKey = config.apiKey();
            var items = request.setKey(apiKey)
                .setMaxResults(25L)
                .setOrder("viewCount")
                .setQ(query)
                .setSafeSearch("none")
                .setType(List.of("video"))
                .execute()
                .getItems();

            List<String> videoIds = items.stream()
                .map(sr -> sr.getId().getVideoId())
                .collect(Collectors.toList());

            // Fetch durations
            var durations = fetchDurations(videoIds);

            return items.stream()
                .map(sr ->
                {
                    SearchResultSnippet snippet = sr.getSnippet();
                    String videoId = sr.getId().getVideoId();
                    long lengthMs = durations.getOrDefault(videoId, -1L);

                    return new AudioTrackInfo(
                        snippet.getTitle(),
                        snippet.getChannelTitle(),
                        lengthMs,
                        videoId,
                        false,
                        "https://www.youtube.com/watch?v=" + videoId
                    );
                })
                .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            logger.error("Failed to get videos from youtube api", e);
        }

        return new ArrayList<>();
    }

    public List<AudioTrackInfo> searchPlaylists(String query)
    {
        try
        {
            YouTube.Search.List request = apiService.search()
                .list(List.of("snippet"));

            String apiKey = config.apiKey();
            return request.setKey(apiKey)
                .setMaxResults(25L)
                .setOrder("viewCount")
                .setQ(query)
                .setSafeSearch("none")
                .setType(List.of("playlist"))
                .execute()
                .getItems()
                .stream()
                .map(sr ->
                {
                    SearchResultSnippet snippet = sr.getSnippet();
                    String playlistId = sr.getId().getPlaylistId();

                    return new AudioTrackInfo(snippet.getTitle(), snippet.getChannelTitle(), -1,
                        playlistId, false, "https://www.youtube.com/playlist?list=" + playlistId);
                })
                .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            logger.error("Failed to get playlists from youtube api", e);
        }

        return new ArrayList<>();
    }

    private Map<String, Long> fetchDurations(List<String> videoIds) throws IOException
    {
        Map<String, Long> result = new java.util.HashMap<>();
        if (videoIds.isEmpty())
        {
            return result;
        }

        // videos.list supports up to 50 IDs per request; we have <=25 so single call is fine
        YouTube.Videos.List req = apiService.videos()
            .list(List.of("contentDetails"))
            .setKey(config.apiKey())
            .setId(videoIds);

        var response = req.execute();
        response.getItems().forEach(video ->
        {
            String id = video.getId();
            String isoDuration = video.getContentDetails().getDuration(); // e.g. PT1H2M30S
            long ms = -1L;
            if (isoDuration != null && !isoDuration.isBlank())
            {
                try
                {
                    // java.time.Duration parses the ISO-8601 format
                    ms = java.time.Duration.parse(isoDuration).toMillis();
                }
                catch (Exception ignored)
                {
                    // keep -1
                }
            }
            result.put(id, ms);
        });

        return result;
    }
}