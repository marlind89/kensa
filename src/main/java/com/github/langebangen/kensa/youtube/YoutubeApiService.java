package com.github.langebangen.kensa.youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.config.YoutubeConfig;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class YoutubeApiService
{
	private static final Logger logger = LoggerFactory.getLogger(YoutubeApiService.class);

	private static final String APPLICATION_NAME = "Kensa";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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

	public List<AudioTrackInfo> searchPlaylists(String query)
	{
		try
		{
			YouTube.Search.List request = apiService.search()
				.list("snippet");

			String apiKey = config.apiKey();
			return request.setKey(apiKey)
				.setMaxResults(25L)
				.setOrder("viewCount")
				.setQ(query)
				.setSafeSearch("none")
				.setType("playlist")
				.execute()
				.getItems()
				.stream()
				.map(sr -> {
					SearchResultSnippet snippet = sr.getSnippet();
					String playlistId = sr.getId().getPlaylistId();

					return new AudioTrackInfo(snippet.getTitle(), snippet.getChannelTitle(), -1,
						playlistId, false, "https://www.youtube.com/playlist?list=" + playlistId);
				})
				.collect(Collectors.toList());
		}
		catch(IOException e)
		{
			logger.error("Failed to get playlists from youtube api", e);
		}

		return new LinkedList<>();
	}
}