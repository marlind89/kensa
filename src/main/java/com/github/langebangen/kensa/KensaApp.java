package com.github.langebangen.kensa;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.util.DiscordException;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.listener.EventListener;
import com.github.langebangen.kensa.listener.RadioListener;
import com.github.langebangen.kensa.listener.TextChannelListener;
import com.github.langebangen.kensa.listener.VoiceChannelListener;
import com.github.langebangen.kensa.module.KensaModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
		long voiceChannelId = 0;
		if (args.length > 0)
		{
			String voiceChannelToConnectTo = args[0];
			if (voiceChannelToConnectTo != null && !voiceChannelToConnectTo.isEmpty())
			{
				voiceChannelId = Long.parseLong(voiceChannelToConnectTo);
			}
		}

		ConfigFilesProvider configFilesProvider = () -> Collections
			.singletonList(Paths.get(System.getProperty("user.dir"), "config.yaml"));

		ConfigurationSource source = new FilesConfigurationSource(configFilesProvider);
		ConfigurationProvider provider = new ConfigurationProviderBuilder()
			.withConfigurationSource(source)
        	.withReloadStrategy(new PeriodicalReloadStrategy(5, TimeUnit.SECONDS))
			.build();

		Injector injector = Guice.createInjector(new KensaModule(voiceChannelId, provider));
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