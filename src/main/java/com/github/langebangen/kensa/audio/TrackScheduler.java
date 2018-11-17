package com.github.langebangen.kensa.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/**
 * Class for scheduling tracks to the {@link AudioPlayer}.
 *
 * @author langen
 */
public class TrackScheduler
	extends AudioEventAdapter
{
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	private boolean loopEnabled;

	/**
	 * Constructor.
	 *
	 * @param player
	 * 		the {@link AudioPlayer}
	 */
	public TrackScheduler(AudioPlayer player)
	{
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.loopEnabled = false;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track
	 * 		the track to play or add to queue.
	 */
	public void queue(AudioTrack track)
	{
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the audioPlayer was already playing so this
		// track goes to the queue instead.
		if(!player.startTrack(track, true))
		{
			queue.offer(track);
		}
	}

	/**
	 * Queues the specified {@link AudioPlaylist}.
	 *
	 * @param playlist
	 * 		the {@link AudioPlaylist} to add to the queue.
	 */
	public void queue(AudioPlaylist playlist)
	{
		for(AudioTrack track : playlist.getTracks())
		{
			queue(track);
		}
	}

	/**
	 * Starts the next track, stopping the current one if it is playing.
	 */
	public void nextTrack()
	{
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the audioPlayer.
		AudioTrack trackToPlay = queue.poll();
		player.startTrack(trackToPlay, false);
	}

	/**
	 * Returns whether the playlist contains any track after
	 * the current one.
	 *
	 * @return
	 * 		if the player has a next track after the current one
	 */
	public boolean hasNextTrack()
	{
		return queue.size() > 0;
	}

	/**
	 * Gets the current {@link AudioTrack} playing, or null
	 * if nothing is playing.
	 *
	 * @return
	 * 		the current {@link AudioTrack} playing, or null
	 * 		if nothing is playing.
	 */
	public AudioTrack getCurrentTrack()
	{
		return player.getPlayingTrack();
	}

	/**
	 * Sets whether the {@link AudioPlayer} should loop the playlist,
	 * i.e. no {@link AudioTrack}s should not be removed from the playlist.
	 *
	 * @param loopEnabled
	 * 		whether looping should be enabled
	 */
	public void setLooping(boolean loopEnabled)
	{
		this.loopEnabled = loopEnabled;
	}

	/**
	 * Shuffles the playlist.
	 */
	public synchronized void shuffle()
	{
		List<AudioTrack> tmpList = new ArrayList<>(queue);
		Collections.shuffle(tmpList);
		queue.clear();
		tmpList.forEach(queue::offer);
	}

	/**
	 * Gets the {@link AudioTrack} playlist
	 *
	 * Note that any changes made to the returned {@link List}
	 * will not affect the actual playlist.
	 *
	 * @return
	 * 		the {@link AudioTrack} playlist.
	 */
	public List<AudioTrack> getPlaylist()
	{
		AudioTrack playingTrack = player.getPlayingTrack();
		List<AudioTrack> playlist = new ArrayList<>();
		if(playingTrack != null)
		{
			playlist.add(playingTrack);
		}
		playlist.addAll(queue);
		return playlist;
	}

	/**
	 * Pauses the player.
	 *
	 * @param pause
	 * 		true to pause, false to play
	 */
	public void pause(boolean pause)
	{
		player.setPaused(pause);
	}

	/**
	 * Returns whether this player is paused
	 *
	 * @return
	 *      whether this player is paused
	 */
	public boolean isPaused()
	{
		return player.isPaused();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
	{
		// Add the track again to the end of the queue if loop is enabled
		if(loopEnabled)
		{
			queue.offer(track.makeClone());
		}
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if(endReason.mayStartNext)
		{
			nextTrack();
		}
	}

	/**
	 * Clears the playlist and stops any track that is playing.
	 */
	public void clear()
	{
		player.stopTrack();
		queue.clear();
	}
}