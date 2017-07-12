package com.github.langebangen.kensa.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import rita.RiMarkov;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.File;

/**
 * @author Martin.
 */
public class KensaModule
	extends AbstractModule
{
	private final String token;

	public KensaModule(String token)
	{
		this.token = token;
	}

	@Override
	protected void configure()
	{
	}

	@Provides
	@Singleton
	public RiMarkov provideMarkov()
	{
		RiMarkov markov = new RiMarkov(3);
		if(new File("messages.txt").isFile())
		{
			markov.loadFrom("messages.txt");
		}
		return markov;
	}

	@Provides
	@Singleton
	public IDiscordClient getDiscordClient()
			throws DiscordException
	{
		return new ClientBuilder()
			.withToken(token)
			.build();
	}
}
