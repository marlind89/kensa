package com.github.langebangen.kensa;

import com.github.langebangen.kensa.listener.RadioListener;
import com.github.langebangen.kensa.listener.TextChannelListener;
import com.github.langebangen.kensa.listener.VoiceChannelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rita.RiMarkov;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.util.DiscordException;

import java.io.File;

/**
 * Main class for Kensa.
 *
 * @author langen
 */
public class KensaApp
{
	private static final Logger logger = LoggerFactory.getLogger(KensaApp.class);

	/**
	 * Main method.
	 *
	 * @param args
	 *      the arguments, should contain the bot token.
	 *
	 * @throws DiscordException
	 */
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

		RiMarkov markov = new RiMarkov(3);
		if(new File("messages.txt").isFile())
		{
			markov.loadFrom("messages.txt");
		}

		dcClient.getDispatcher().registerListener(new EventListener(dcClient, markov));
		dcClient.getDispatcher().registerListener(new RadioListener(dcClient));
		dcClient.getDispatcher().registerListener(new TextChannelListener(dcClient, markov));
		dcClient.getDispatcher().registerListener(new VoiceChannelListener(dcClient));

		dcClient.login();

		logger.info("Opus version:" + Opus.INSTANCE.opus_get_version_string());
	}
}