package com.github.langebangen.kensa;

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
	PLAY     ("play", "Queues the specified song in the playlist from the specified URL. This play function supports youtube links and urls that ends with .mp3, .ogg, .flac, or .wav."),
	SKIP     ("skip", "Skips the current song and additional future songs if a number is provided."),
	SONG     ("song", "Shows the current track."),
	PLAYLIST ("playlist", "Shows the playlist."),
	CLEAR    ("clear", "Clears the playlist.");

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
