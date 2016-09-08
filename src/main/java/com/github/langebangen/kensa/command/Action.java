package com.github.langebangen.kensa.command;

/**
 * Class describing the actions Kensa supports.
 *
 * @author langen
 */
public enum Action
{
	HELP     ("help", "Shows this help description."),
	JOIN     ("join", "Joins the specified voice channel."),
	LEAVE    ("leave", "Leaves the current channel Kensa is in."),
	PLAY     ("play", "Queues the specified song in the playlist from the specified URL. This play function supports youtube links " +
			          "and urls that ends with .mp3, .ogg, .flac, or .wav. If the argument is neither of this then it will search on youtube " +
			          "and queue the song which had the best match."),
	SKIP     ("skip", "Skips the current song and additional future songs if a number is provided."),
	SONG     ("song", "Shows the current track."),
	LOOP     ("loop", "Enables/disables looping of the playlist. 'on' to enable looping and 'off' to disable it. Disabled by default."),
	SHUFFLE  ("shuffle", "Shuffles the playlist."),
	PLAYLIST ("playlist", "Shows the playlist."),
	PAUSE    ("pause", "Pauses the music player. 'on' to pause and 'off' to resume the player."),
	SEARCH   ("search", "Search and display the eight best matches from youtube."),
	CLEAR    ("clear", "Clears the playlist."),
	BABYLON  ("babylon", "Chooses a delicious babylon dish for you so you don't have to!");

	private final String action;
	private final String description;

	/**
	 * Constructor.
	 *
	 * @param command
	 *      the command
	 * @param description
	 *      the description
	 */
	Action(String command, String description)
	{
		this.action = "!" + command;
		this.description = description;
	}

	/**
	 * Gets the action
	 *
	 * @return action
	 */
	public String getAction()
	{
		return action;
	}

	/**
	 * Gets the description
	 *
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Gets the action for the specified action string
	 *
	 * @param actionValue
	 *      the action value string
	 *
	 * @return action
	 *      the action that matched the action value,
	 *      or null if there was no match.
	 */
	public static Action getAction(String actionValue)
	{
		for(Action command : values())
		{
			if(command.action.equalsIgnoreCase(actionValue))
			{
				return command;
			}
		}

		return null;
	}

}