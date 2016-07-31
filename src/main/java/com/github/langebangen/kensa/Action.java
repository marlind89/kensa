package com.github.langebangen.kensa;

/**
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
	private final int argsAmount;
	private final String description;

	Action(String command, String description)
	{
		this(command, 0, description);
	}

	Action(String command, int argsAmount, String description)
	{
		this.action = "!" + command;
		this.argsAmount = argsAmount;
		this.description = description;
	}

	public String getAction()
	{
		return action;
	}

	public String getDescription()
	{
		return description;
	}

	public static Action getAction(String commandValue)
	{
		for(Action command : values())
		{
			if(command.action.equalsIgnoreCase(commandValue))
			{
				return command;
			}
		}

		return null;
	}

}
