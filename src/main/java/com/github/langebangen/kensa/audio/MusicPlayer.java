package com.github.langebangen.kensa.audio;

import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Main interface for interacting with the music player.
 * This {@link MusicPlayer} may be created with the {@link MusicPlayerManager}
 *
 * Every {@link IGuild} has its own {@link MusicPlayer} which will be located
 * inside the {@link MusicPlayerManager}
 *
 * @author langen
 */
public interface MusicPlayer
{
    /**
     * Streams the content located on the specified URL to the specified audioPlayer.
     * Will send a message that the content has been added to the playlist queue.
     *
     * @param urlString
     *      the url string
     * @param channel
     *      the {@link IChannel}
     */
    void stream(String urlString, IChannel channel);

    /**
     * Searchs youtube with the specified {@link SearchYoutubeEvent}
     * and prints out the results to the channel associated with the
     * specified event.
     *
     * @param event
     *      the {@link SearchYoutubeEvent}
     */
    void searchYoutube(SearchYoutubeEvent event);

    /**
     * Skips the current track, if any
     */
    void skipTrack();

    /**
     * Skips the specfied amount of tracks
     *
     * @param skipAmount
     *      the amount of tracks to skip
     */
    void skipTrack(int skipAmount);

    /**
     * Sets whether looping should be enabled.
     *
     * @param loopEnabled
     *      true to loop the playlist, false otherwise
     */
    void setLoopEnabled(boolean loopEnabled);

    /**
     * Shuffles the playlist
     */
    void shuffle();

    /**
     * Gets the current {@link AudioTrack} playing, or null
     * if nothing is playing.
     *
     * @return
     *      the current {@link AudioTrack}, or null if nothing is playing
     */
    AudioTrack getCurrentTrack();

    /**
     * Gets the playlist
     *
     * @return
     *      the {@link AudioTrack} playlist
     */
    List<AudioTrack> getPlayList();

    /**
     * Clears the playlist
     */
    void clearPlaylist();

    /**
     * Pauses the player
     *
     * @param pause
     *      true to pause, false to play
     */
    void pause(boolean pause);
}