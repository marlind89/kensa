package com.github.langebangen.kensa.command;

import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import com.github.langebangen.kensa.role.KensaRole;

/**
 * Class describing the actions Kensa supports.
 *
 * @author langen
 */
public enum Action
{
	HELP      ("help", "Shows this help description."),
	JOIN      ("join", "Joins the specified voice channel."),
	LEAVE     ("leave", "Leaves the current channel Kensa is in."),
	PLAY      ("play", "Queues the specified song in the playlist from the specified URL. This play function supports streaming from several sites such"
		+ " as youtube, twitch, soundcloud, bandcamp, vimeo and direct links to tracks. You can also queue spotify playlists by providing the spotify URI to the playlist."
		+ " If the identity provided wasn't found on any of the supported sites, a youtube search is performed and the first match is queued. Add '-p' before the"
		+ " identity to search and queue the first playlist match from youtube. If no playlist was found, kensa will perform a normal youtube search and compile"
		+ " a playlist of the matches."),
	SKIP      ("skip", "Skips the current song and additional future songs if a number is provided."),
	SONG      ("song", "Shows the current track."),
	LOOP      ("loop", "Enables/disables looping of the playlist. 'on' to enable looping and 'off' to disable it. Disabled by default."),
	SHUFFLE   ("shuffle", "Shuffles the playlist."),
	PLAYLIST  ("playlist", "Shows the playlist."),
	PAUSE     ("pause", "Pauses the music audioPlayer. 'on' to pause and 'off' to resume the audioPlayer."),
	SEARCH    ("search", "Search and display the best matches from youtube. Add '-p' to search for playlists only."),
	CLEAR     ("clear", "Clears the playlist."),
	BABYLON   ("babylon", "Chooses a delicious babylon dish for you so you don't have to!"),
	INSULT    ("insult", "Insults the specified person. The person should be mentioned for this to work. Use !insult add to add an insult. " +
			          "!insult remove to remove the previous insult from the insult list"),
	RECONNECT ("reconnect", "Reconnects to the current voice channel. Useful if Kensa can't play music."),
	RESTART   ("restart", "Restarts kensa", KensaRole.ADMIN);

	private final String command;
	private final String description;
	private final KensaRole requiresRole;

	Action(String command, String description)
	{
		this(command, description, null);
	}

	Action(String command, String description, KensaRole requiresRole)
	{
		this.command = command;
		this.description = description;
		this.requiresRole = requiresRole;
	}

	public String getCommand()
	{
		return command;
	}
	
	/**
	 * Gets the action
	 *
	 * @return action
	 */
	public String getAction()
	{
		return "!" + command;
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
			if(command.getAction().equalsIgnoreCase(actionValue))
			{
				return command;
			}
		}

		return null;
	}

	public Mono<Boolean> hasPermission(Member member)
	{
		return requiresRole == null
			? Mono.just(true)
			: member.getRoles().any(role -> role.getName().equals(requiresRole.GetRoleName()));
	}
}