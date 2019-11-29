package com.github.langebangen.kensa;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import discord4j.core.DiscordClient;
import reactor.core.publisher.Hooks;

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
	 */
	public static void main(String[] args)
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

		Hooks.onOperatorDebug();

		Injector injector = Guice.createInjector(new KensaModule(voiceChannelId, provider));
		DiscordClient dcClient = injector.getInstance(DiscordClient.class);
		try
		{
			registerListeners(injector);
		}
		catch(Exception e){
			System.out.println(e);
		}

		dcClient.login().block();
	}

	private static void registerListeners(Injector injector)
	{
		injector.getInstance(RadioListener.class);
		injector.getInstance(EventListener.class);
		injector.getInstance(TextChannelListener.class);
		injector.getInstance(VoiceChannelListener.class);
	}
}