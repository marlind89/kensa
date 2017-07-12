package com.github.langebangen.kensa.audio;

import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.MessageBuilder;

import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

/**
 * Implementation of {@link MusicPlayer} using the lava player library
 *
 * @author langen
 */
public class LavaMusicPlayer
    implements MusicPlayer
{
    private final TrackScheduler trackScheduler;
    private final AudioPlayerManager playerManager;
	private final YoutubeSearchProvider ytSearchProvider;

    public LavaMusicPlayer(TrackScheduler trackScheduler,
		AudioPlayerManager playerManager,
		YoutubeSearchProvider ytSearchProvider)
    {
        this.trackScheduler = trackScheduler;
        this.playerManager = playerManager;
        this.ytSearchProvider = ytSearchProvider;
    }

    @Override
    public void stream(String urlString, IChannel channel)
    {
        loadTrack(urlString, channel);
    }

	@Override
	public void searchYoutube(SearchYoutubeEvent event)
	{
		AudioItem audioItem = ytSearchProvider.loadSearchResult(event.getSearchQuery());

		MessageBuilder messageBuilder = new MessageBuilder(event.getClient())
			.withChannel(event.getTextChannel())
			.appendContent("```");

		if(audioItem instanceof BasicAudioPlaylist)
		{
			for(AudioItem item : ((BasicAudioPlaylist)audioItem).getTracks())
			{
				if(item instanceof YoutubeAudioTrack)
				{
					YoutubeAudioTrack ytTrack = ((YoutubeAudioTrack)item);
					String youtubeId = ytTrack.getIdentifier();
					String title = ytTrack.getInfo().title;
					String duration = TrackUtils.getReadableDuration(ytTrack.getDuration());
					messageBuilder.appendContent(youtubeId);
					messageBuilder.appendContent(" - " + title + " [" + duration + "]\n");
				}
			}
		}
		messageBuilder.appendContent("```");
		messageBuilder.send();
	}

	@Override
	public void skipTrack()
	{
		trackScheduler.nextTrack();
	}

	@Override
	public void skipTrack(int skipAmount)
	{
		skipAmount = Math.max(0, skipAmount);
		for(int i = 0; i < skipAmount; i++)
		{
			skipTrack();
		}
	}

	@Override
	public AudioTrack getCurrentTrack()
	{
		return trackScheduler.getCurrentTrack();
	}

	@Override
	public List<AudioTrack> getPlayList()
	{
		return trackScheduler.getPlaylist();
	}

	@Override
	public void clearPlaylist()
	{
		trackScheduler.clear();
	}

	@Override
	public void pause(boolean pause)
	{
		trackScheduler.pause(pause);
	}

	@Override
	public void setLoopEnabled(boolean loopEnabled)
	{
		trackScheduler.setLooping(loopEnabled);
	}

	@Override
	public void shuffle()
	{
		trackScheduler.shuffle();
	}

    private void loadTrack(String songIdentity, IChannel channel)
    {
		playerManager.loadItemOrdered(trackScheduler, songIdentity, new AudioLoadResultHandler()
        {
        	private boolean fallbackSearchPerformed = false;

            @Override
            public void trackLoaded(AudioTrack track)
            {
                String readableTrack = TrackUtils.getReadableTrack(track);

                new MessageBuilder(channel.getClient())
                    .withChannel(channel)
                    .appendContent("Queued ")
                    .appendContent(readableTrack, MessageBuilder.Styles.BOLD)
                    .build();

                trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
            	if(fallbackSearchPerformed)
				{
					// We will arrive here upon youtube search fallbacks,
					// but we only want to queue the first match in this case
					AudioTrack audioTrack = playlist.getTracks().get(0);
					loadTrack(audioTrack.getIdentifier(), channel);
				}
				else
				{
					new MessageBuilder(channel.getClient())
						.withChannel(channel)
						.appendContent("Queuing " + playlist.getTracks().size() + " songs..")
						.build();

					trackScheduler.queue(playlist);
				}
            }

            @Override
            public void noMatches()
            {
            	if(!fallbackSearchPerformed && !songIdentity.startsWith("http"))
				{
					playerManager.loadItemOrdered(trackScheduler,"ytsearch:" + songIdentity, this);
					fallbackSearchPerformed = true;
				}
				else
				{
					new MessageBuilder(channel.getClient())
						.withChannel(channel)
						.appendContent("Nope couldn't find that..")
						.build();
				}
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                new MessageBuilder(channel.getClient())
                    .withChannel(channel)
                    .appendContent(exception.getMessage())
                    .build();
            }
        });
    }
}
