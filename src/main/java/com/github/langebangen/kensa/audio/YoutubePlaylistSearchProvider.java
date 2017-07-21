package com.github.langebangen.kensa.audio;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

/**
 * Provider for searching playlists on youtube
 *
 * @author langen
 */
public class YoutubePlaylistSearchProvider
{
	private final YoutubeAudioSourceManager sourceManager;

	/**
	 * Constructor.
	 *
	 * @param sourceManager
	 * 		the youtube source manager used for getting the youtube http interface.
	 */
	public YoutubePlaylistSearchProvider(YoutubeAudioSourceManager sourceManager)
	{
		this.sourceManager = sourceManager;
	}

	/**
	 * Search for playlists
	 *
	 * @param query
	 * 		the playlist search query
	 *
	 * @return
	 * 		the playlists of the first page of results.
	 */
	public List<AudioTrackInfo> searchPlaylists(String query)
	{
		try(HttpInterface httpInterface = sourceManager.getHttpInterface())
		{
			URI url = new URIBuilder("https://www.youtube.com/results")
				.addParameter("q", query)
				.addParameter("sp", "EgIQAw==").build();

			try(CloseableHttpResponse response = httpInterface.execute(new HttpGet(url)))
			{
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode != 200)
				{
					throw new IOException("Invalid status code for search response: " + statusCode);
				}

				Document document = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "");
				return extractSearchResults(document);
			}
		}
		catch(Exception e)
		{
			throw ExceptionTools.wrapUnfriendlyExceptions(e);
		}
	}

	private List<AudioTrackInfo> extractSearchResults(Document document)
	{
		List<AudioTrackInfo> playlists = new ArrayList<>();

		for(Element results : document.select("#page > #content #results"))
		{
			for(Element result : results.select(".yt-lockup-playlist"))
			{
				if(!result.hasAttr("data-ad-impressions")
					&& result.select(".standalone-ypc-badge-renderer-label").isEmpty())
				{
					extractTrackFromResultEntry(playlists, result);
				}
			}
		}

		return playlists;
	}

	private void extractTrackFromResultEntry(List<AudioTrackInfo> playlists, Element element)
	{
		Element playlistElement = element.select(".addto-watch-queue-play-now").first();
		String playlistId = playlistElement != null
			? playlistElement.attr("data-list-id")
			: null;
		String playlistName = element.select(".yt-lockup-title > a").text();
		String author = element.select(".yt-lockup-byline > a").text();

		// TODO: Calculate the playlist duration by iterating through the
		// yt-lockup-playlist-items (if it is of any interest).

		if(playlistId != null)
		{
			AudioTrackInfo trackInfo = new AudioTrackInfo(playlistName, author, -1,
				playlistId, false, "https://www.youtube.com/playlist?list=" + playlistId);
			playlists.add(trackInfo);
		}
	}
}