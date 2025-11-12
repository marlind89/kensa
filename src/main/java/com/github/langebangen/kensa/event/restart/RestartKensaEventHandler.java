package com.github.langebangen.kensa.event.restart;

import com.github.langebangen.kensa.audio.VoiceConnections;
import com.github.langebangen.kensa.event.EventHandler;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestartKensaEventHandler implements EventHandler<RestartKensaEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(RestartKensaEventHandler.class);

    private final VoiceConnections voiceConnections;

    @Inject
    public RestartKensaEventHandler(VoiceConnections voiceConnections)
    {
        this.voiceConnections = voiceConnections;
    }

    @Override
    public Publisher<?> handle(RestartKensaEvent event)
    {
        return voiceConnections.disconnect(event.getTextChannel().getGuildId())
            .flatMap(vcc ->
            {
                String voiceChannelId = vcc == null ? "" : " " + vcc.audioChannel().getId().asLong();
                return event.getTextChannel().createMessage("Restarting...")
                    .then(event.getClient().logout())
                    .thenReturn(voiceChannelId);
            })
            .doOnNext(voiceChannelId ->
            {
                List<String> command = new ArrayList<>();
                command.add("/bin/bash");
                command.add("-c");
                command.add("sleep 5 && ./kensa.sh" + voiceChannelId);
                ProcessBuilder builder = new ProcessBuilder(command);

                try
                {
                    builder.start();
                    System.exit(0);
                }
                catch (IOException e)
                {
                    logger.error("Failed to restart Kensa!", e);
                }
            });
    }
}
