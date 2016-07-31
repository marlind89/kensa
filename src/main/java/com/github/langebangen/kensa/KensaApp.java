package com.github.langebangen.kensa;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * @author langen
 */
public class KensaApp
{
	private static final Logger logger = LoggerFactory.getLogger(KensaApp.class);

	public static void main(String[] args)
		throws DiscordException
	{
		if(args.length != 1)
		{
			System.out.println("The bot token must be provided in main args.");
			System.exit(0);
		}

		final IDiscordClient dcClient = new ClientBuilder()
				.withToken(args[0])
				.build();

		dcClient.getDispatcher().registerListener(new EventListener(dcClient));
		dcClient.login();

		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			try
			{
				dcClient.logout();
			}
			catch (DiscordException | RateLimitException e)
			{
				logger.error("Error logging out.", e);
			}
		}));
	}
}