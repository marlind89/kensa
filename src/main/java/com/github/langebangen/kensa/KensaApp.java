package com.github.langebangen.kensa;

import com.github.langebangen.kensa.job.GuiceJobFactory;
import com.github.langebangen.kensa.job.UpdateMessagesOnDiskJob;
import com.github.langebangen.kensa.listener.EventListener;
import com.github.langebangen.kensa.listener.RadioListener;
import com.github.langebangen.kensa.listener.TextChannelListener;
import com.github.langebangen.kensa.listener.VoiceChannelListener;
import com.github.langebangen.kensa.module.KensaModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import discord4j.core.GatewayDiscordClient;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Hooks;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
     * @param args the arguments, should contain the bot token.
     */
    public static void main(String[] args)
    {
        try
        {
            long voiceChannelId = 0;
            if (args.length > 0)
            {
                String voiceChannelToConnectTo = args[0];
                if (voiceChannelToConnectTo != null && !voiceChannelToConnectTo.isEmpty())
                {
                    voiceChannelId = Long.parseLong(voiceChannelToConnectTo);
                    logger.info("Started with an existing voice channel id: " + voiceChannelId);
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
            GatewayDiscordClient gateway = injector.getInstance(GatewayDiscordClient.class);

            registerListeners(injector);
            initializeScheduler(injector);

            Runtime.getRuntime().addShutdownHook(new Thread(
                () -> gateway.logout().block(Duration.ofSeconds(10)))
            );

            gateway.onDisconnect().block();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }

    private static void initializeScheduler(Injector injector)
        throws SchedulerException
    {
        var factory = new StdSchedulerFactory();
        var scheduler = factory.getScheduler();

        scheduler.setJobFactory(new GuiceJobFactory(injector));

        var job = JobBuilder.newJob(UpdateMessagesOnDiskJob.class)
            .withIdentity("updateMessagesOnDiskJob", "group1")
            .build();

        var immediateTrigger = TriggerBuilder.newTrigger()
            .withIdentity("immediateTrigger", "group1")
            .startNow()
            .build();

        var dailyTrigger = TriggerBuilder.newTrigger()
            .withIdentity("dailyTrigger", "group1")
            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(4, 0))
            .build();

        scheduler.scheduleJob(job, Set.of(immediateTrigger, dailyTrigger), true);

        scheduler.start();
    }

    private static void registerListeners(Injector injector)
    {
        injector.getInstance(EventListener.class);
        injector.getInstance(RadioListener.class);
        injector.getInstance(TextChannelListener.class);
        injector.getInstance(VoiceChannelListener.class);
    }
}