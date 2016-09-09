package com.github.langebangen.kensa.listener;

import com.github.langebangen.kensa.babylon.Babylon;
import com.github.langebangen.kensa.command.Action;
import com.github.langebangen.kensa.listener.event.BabylonEvent;
import com.github.langebangen.kensa.listener.event.HelpEvent;
import com.github.langebangen.kensa.util.KensaConstants;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.util.MessageBuilder;

/**
 * @author Martin.
 */
public class TextChannelListener
	extends AbstractEventListener
{
	public TextChannelListener(IDiscordClient client)
	{
		super(client);
	}

	@EventSubscriber
	public void onHelpEvent(HelpEvent event)
	{
		MessageBuilder messageBuilder = new MessageBuilder(client)
				.withChannel(event.getTextChannel());

		messageBuilder.appendContent("Kensa v" + KensaConstants.VERSION, MessageBuilder.Styles.ITALICS);
		messageBuilder.appendContent("\n");
		for(Action action : Action.values())
		{
			messageBuilder.appendContent("\n" + action.getAction(), MessageBuilder.Styles.BOLD);
			messageBuilder.appendContent(" - " + action.getDescription());
		}
		sendMessage(messageBuilder);
	}

	@EventSubscriber
	public void onBabylonEvent(BabylonEvent event)
	{
		sendMessage(event.getTextChannel(), "```" + Babylon.INSTANCE.getRandomDish() + "```");
	}
}