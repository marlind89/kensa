package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.command.Action;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * @author Martin.
 */
public abstract class AbstractEventListener
{
	protected final IDiscordClient client;

	protected AbstractEventListener(IDiscordClient client)
	{
		this.client = client;
	}

	/**
	 * Sends the help message.
	 *
	 * @param channel
	 *      the {@link IChannel}
	 */
	protected void sendHelpMessage(IChannel channel)
	{
		MessageBuilder builder = new MessageBuilder(client)
				.withChannel(channel);

		for(Action action : Action.values())
		{
			builder.appendContent("\n" + action.getAction(), MessageBuilder.Styles.BOLD);
			builder.appendContent(" - " + action.getDescription());
		}

		sendMessage(builder);
	}

	/**
	 * Sends the specified message.
	 *
	 * @param channel
	 *      the {@link IChannel}
	 * @param message
	 *      the message
	 */
	protected void sendMessage(IChannel channel, String message)
	{
		MessageBuilder builder = new MessageBuilder(client)
				.withChannel(channel)
				.withContent(message);

		sendMessage(builder);
	}

	/**
	 * Sends the message which has been created in the
	 * specified {@link MessageBuilder}
	 *
	 * @param messageBuilder
	 *      the {@link MessageBuilder}
	 */
	protected void sendMessage(MessageBuilder messageBuilder)
	{
		try
		{
			messageBuilder.send();
		}
		catch(DiscordException | RateLimitException | MissingPermissionsException e)
		{
			e.printStackTrace();
		}
	}
}