package com.github.langebangen.kensa;

import com.github.langebangen.kensa.listener.EventListener;
import com.github.langebangen.kensa.listener.RadioListener;
import com.github.langebangen.kensa.listener.TextChannelListener;
import com.github.langebangen.kensa.listener.VoiceChannelListener;
import com.github.langebangen.kensa.module.KensaModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;

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
		if(args.length < 1)
		{
			System.out.println("The bot token must be provided in main args.");
			System.exit(0);
		}

		long voiceChannelId = 0;
		if (args.length > 1)
		{
			String voiceChannelToConnectTo = args[1];
			if (voiceChannelToConnectTo != null && !voiceChannelToConnectTo.isEmpty())
			{
				voiceChannelId = Long.parseLong(voiceChannelToConnectTo);
			}
		}

		Injector injector = Guice.createInjector(new KensaModule(args[0], voiceChannelId));

		IDiscordClient dcClient = injector.getInstance(IDiscordClient.class);
		registerListeners(dcClient, injector);
		dcClient.login();


		logger.info("Opus version:" + Opus.INSTANCE.opus_get_version_string());
	}

	private static void registerListeners(IDiscordClient dcClient, Injector injector)
	{
		EventDispatcher dispatcher = dcClient.getDispatcher();
		dispatcher.registerListener(injector.getInstance(EventListener.class));
		dispatcher.registerListener(injector.getInstance(RadioListener.class));
		dispatcher.registerListener(injector.getInstance(TextChannelListener.class));
		dispatcher.registerListener(injector.getInstance(VoiceChannelListener.class));
	}
}