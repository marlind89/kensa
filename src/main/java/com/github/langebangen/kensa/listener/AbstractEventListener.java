package com.github.langebangen.kensa.listener;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;

import com.github.langebangen.kensa.command.Action;

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
	 *
	 * @return
	 * 		the sent {@link IMessage}
	 */
	protected IMessage sendMessage(IChannel channel, String message)
	{
		MessageBuilder builder = new MessageBuilder(client)
				.withChannel(channel)
				.withContent(message);

		return sendMessage(builder);
	}

	/**
	 * Sends the message which has been created in the
	 * specified {@link MessageBuilder}
	 *
	 * @param messageBuilder
	 *      the {@link MessageBuilder}
	 *
	 * @return
	 * 		the sent {@link IMessage}
	 */
	protected IMessage sendMessage(MessageBuilder messageBuilder)
	{
		return messageBuilder.send();
	}
}