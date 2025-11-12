package com.github.langebangen.kensa.event.dispatcher;

import com.github.langebangen.kensa.command.Command;
import com.github.langebangen.kensa.event.EventHandler;
import com.github.langebangen.kensa.event.babylon.BabylonEvent;
import com.github.langebangen.kensa.event.help.HelpEvent;
import com.github.langebangen.kensa.event.insult.InsultEvent;
import com.github.langebangen.kensa.event.insult.SaveInsultEvent;
import com.github.langebangen.kensa.event.radio.playlist.clear.ClearPlaylistEvent;
import com.github.langebangen.kensa.event.radio.playlist.loop.LoopPlaylistEvent;
import com.github.langebangen.kensa.event.radio.playlist.show.ShowPlaylistEvent;
import com.github.langebangen.kensa.event.radio.playlist.shuffle.ShufflePlaylistEvent;
import com.github.langebangen.kensa.event.radio.track.current.CurrentTrackEvent;
import com.github.langebangen.kensa.event.radio.track.pause.PauseTrackEvent;
import com.github.langebangen.kensa.event.radio.track.play.PlayTrackEvent;
import com.github.langebangen.kensa.event.radio.track.skip.SkipTrackEvent;
import com.github.langebangen.kensa.event.restart.RestartKensaEvent;
import com.github.langebangen.kensa.event.search.SearchYoutubeEvent;
import com.github.langebangen.kensa.event.voicechannel.join.JoinVoiceChannelEvent;
import com.github.langebangen.kensa.event.voicechannel.leave.LeaveVoiceChannelEvent;
import com.github.langebangen.kensa.event.voicechannel.reconnect.ReconnectVoiceChannelEvent;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class KensaEventDispatcher implements EventHandler<MessageCreateEvent>
{
    @Override
    public Publisher<?> handle(MessageCreateEvent event)
    {
        var message = event.getMessage();
        var messageCommand = Command.parseCommand(message.getContent());

        return Mono.zip(Mono.justOrEmpty(messageCommand),
                message.getAuthorAsMember().flatMap(member -> messageCommand.getAction().hasPermission(member)),
                message.getGuild(),
                message.getChannel().ofType(TextChannel.class),
                message.getAuthorAsMember())
            .flatMap(zip ->
            {
                Command command = zip.getT1();
                boolean hasPermission = zip.getT2();
                Guild guild = zip.getT3();
                TextChannel channel = zip.getT4();
                Member member = zip.getT5();
                String argument = command.getArgument();
                var client = channel.getClient();

                if (!hasPermission)
                {
                    channel.createMessage("You don't have permission do to that, you filthy fool!").subscribe();
                    return Mono.empty();
                }

                return switch (command.getAction())
                {
                    /* Text channel commands */
                    case HELP -> Mono.just(new HelpEvent(client, channel));
                    case BABYLON -> Mono.just(new BabylonEvent(client, channel));
                    case INSULT ->
                    {
                        String[] insultArgs = argument.split(" ");
                        String insultType = insultArgs[0];
                        if (insultType.startsWith("<"))
                        {
                            String userId = insultType.replaceAll("[^\\d]", "");
                            yield guild.getMemberById(Snowflake.of(Long.parseLong(userId)))
                                .map(user -> new InsultEvent(client, channel, user));
                        }
                        else if (insultType.equals("add"))
                        {
                            String insult = StringUtils.join(Arrays.copyOfRange(insultArgs, 1, insultArgs.length), " ");
                            yield Mono.just(new SaveInsultEvent(client, channel, true, insult));
                        }
                        else if (insultType.equals("remove"))
                        {
                            yield Mono.just(new SaveInsultEvent(client, channel, false, null));
                        }
                        yield Mono.empty();
                    }
                    /* Voice channel commands */
                    case JOIN -> Mono.just(new JoinVoiceChannelEvent(client, channel, argument, member));
                    case LEAVE -> Mono.just(new LeaveVoiceChannelEvent(client, channel));
                    case RECONNECT -> Mono.just(new ReconnectVoiceChannelEvent(client, channel));
                    /* Music player commands */
                    case PLAY ->
                    {
                        String playArg = argument.replace("-p ", "");
                        yield Mono.just(
                            new PlayTrackEvent(client, channel, playArg, !playArg.equals(argument), member, false));
                    }
                    case SKIP -> Mono.just(new SkipTrackEvent(client, channel, argument));
                    case SONG -> Mono.just(new CurrentTrackEvent(client, channel));
                    case LOOP -> Mono.just(new LoopPlaylistEvent(client, channel, argument));
                    case SHUFFLE -> Mono.just(new ShufflePlaylistEvent(client, channel));
                    case PLAYLIST -> Mono.just(new ShowPlaylistEvent(client, channel));
                    case PAUSE -> Mono.just(new PauseTrackEvent(client, channel, argument));
                    case SEARCH ->
                    {
                        if (argument != null)
                        {
                            var searchArg = argument.replace("-p ", "");
                            if (!searchArg.trim().isEmpty())
                            {
                                yield Mono.just(
                                    new SearchYoutubeEvent(client, channel, searchArg, !searchArg.equals(argument)));
                            }
                        }
                        yield Mono.empty();
                    }
                    case CLEAR -> Mono.just(new ClearPlaylistEvent(client, channel));
                    /* Misc commands */
                    case RESTART -> Mono.just(new RestartKensaEvent(client, channel));
                    default -> Mono.empty();
                };
            })
            .doOnNext(e -> e.getClient().getEventDispatcher().publish(e));
    }
}
