package com.github.langebangen.kensa.job;

import com.google.inject.Injector;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class GuiceJobFactory implements JobFactory
{
    private final Injector injector;

    public GuiceJobFactory(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)
    {
        return injector.getInstance(bundle.getJobDetail().getJobClass());
    }
}