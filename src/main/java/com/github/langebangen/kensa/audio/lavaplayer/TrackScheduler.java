package com.github.langebangen.kensa.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Class for scheduling tracks to the {@link AudioPlayer}.
 *
 * @author langen
 */
public class TrackScheduler
	extends AudioEventAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

	private final AudioPlayer player;
	private final LinkedBlockingDeque<AudioTrack> queue;
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
		this.queue = new LinkedBlockingDeque<>();
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
		logger.info("Queuing " + track.getIdentifier());

		track.setUserData(new AudioTrackData(true, false));
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the audioPlayer was already playing so this
		// track goes to the queue instead.
		if(!player.startTrack(track, true))
		{
			queue.offer(track);
		}
	}

	public void playImmediately(AudioTrack track)
	{
		logger.info("Playing immediately " + track.getIdentifier());

		if (player.getPlayingTrack() != null)
		{
			if (player.getPlayingTrack().getUserData() != null)
			{
				((AudioTrackData) player.getPlayingTrack()
					.getUserData()).wasAbortedByImmediatePlay = true;
			}
		}

		track.setUserData(new AudioTrackData(false, false));
		player.startTrack(track, false);
	}

	/**
	 * Queues the specified {@link AudioPlaylist}.
	 *
	 * @param playlist
	 * 		the {@link AudioPlaylist} to add to the queue.
	 */
	public void queue(AudioPlaylist playlist)
	{
		logger.info("Queuing playlist: " + playlist.getName());
		for(AudioTrack track : playlist.getTracks())
		{
			logger.info("Queuing playlist song: " + track.getIdentifier());
			queue(track);
		}
	}

	/**
	 * Starts the next track, stopping the current one if it is playing.
	 */
	public void nextTrack()
	{
		logger.info("Starting next track");
		// Start the next track, regardless of something is already playing or not. In case queue was empty, we are
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
		logger.info("Shuffling playlist");
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
		logger.info("Track ended");

		var isLoopable = true;
		if (track.getUserData() != null)
		{
			var trackData = (AudioTrackData) track.getUserData();

			if (trackData.wasAbortedByImmediatePlay)
			{
				// We end up here if an immediately played track replaced an on going track.
				// Then we want to add the aborted track at the head of the queue again
				// to be replayed at the position it was aborted at, once the immediately played track are finished.
				var cloned = track.makeClone();
				cloned.setPosition(track.getPosition());
				queue.addFirst(cloned);

				trackData.wasAbortedByImmediatePlay = false;
				return;
			}

			isLoopable = trackData.isLoopable;
		}

		// Add the track again to the end of the queue if loop is enabled
		if(loopEnabled && isLoopable)
		{
			logger.info("Looping enabled, add track to end of queue again.");
			queue.offer(track.makeClone());
		}

		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if(endReason.mayStartNext)
		{
			nextTrack();
		}
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs)
	{
		logger.info("Track is stuck! Start next song");
		nextTrack();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception)
	{
		logger.error("Error in onTrackException", exception);
		super.onTrackException(player, track, exception);
	}

	/**
	 * Clears the playlist and stops any track that is playing.
	 */
	public void clear()
	{
		player.stopTrack();
		queue.clear();
	}

	private static class AudioTrackData
	{
		public boolean isLoopable;
		public boolean wasAbortedByImmediatePlay;

		public AudioTrackData(boolean isLoopable,
			boolean wasAbortedByImmediatePlay)
		{
			this.isLoopable = isLoopable;
			this.wasAbortedByImmediatePlay = wasAbortedByImmediatePlay;
		}
	}
}