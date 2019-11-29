package com.github.langebangen.kensa.listener;

import java.util.List;
import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.github.langebangen.kensa.audio.lavaplayer.MusicPlayerManager;
import com.github.langebangen.kensa.listener.event.ClearPlaylistEvent;
import com.github.langebangen.kensa.listener.event.CurrentTrackRequestEvent;
import com.github.langebangen.kensa.listener.event.KensaEvent;
import com.github.langebangen.kensa.listener.event.LoopPlaylistEvent;
import com.github.langebangen.kensa.listener.event.PauseEvent;
import com.github.langebangen.kensa.listener.event.PlayAudioEvent;
import com.github.langebangen.kensa.listener.event.SearchYoutubeEvent;
import com.github.langebangen.kensa.listener.event.ShowPlaylistEvent;
import com.github.langebangen.kensa.listener.event.ShufflePlaylistEvent;
import com.github.langebangen.kensa.listener.event.SkipTrackEvent;
import com.github.langebangen.kensa.util.TrackUtils;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * @author Martin.
 */
public class RadioListener
	extends AbstractEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(RadioListener.class);
	private final MusicPlayerManager playerFactory;

	private static final String PLAY_PAUSE_EMOJI = "\u23EF";
	private static final String NEXT_TRACK_EMOJI = "\u23ed";

	@Inject
	public RadioListener(DiscordClient client, MusicPlayerManager playerFactory)
	{
		super(client);
		this.playerFactory = playerFactory;

		handlePlayAudioEvent();
		handleSearchYoutubeEvent();
		handleSkipTrackEvent();
		handleCurrentTrackRequestEvent();
		handleLoopPlaylistEvent();
		handleShuffleEvent();
		handleShowPlaylistEvent();
		handleClearPlaylistEvent();
		handlePauseEvent();
		handleReactionEvent();
	}

	private void handlePlayAudioEvent()
	{
		dispatcher.on(PlayAudioEvent.class)
			.subscribe(event -> getPlayer(event).ifPresent(player -> player.stream(event)));
	}

	private void handleSearchYoutubeEvent()
	{
		dispatcher.on(SearchYoutubeEvent.class)
			.subscribe(event -> getPlayer(event).ifPresent(player -> player.searchYoutube(event)));
	}

	private void handleSkipTrackEvent()
	{
		dispatcher.on(SkipTrackEvent.class)
			.subscribe(event -> {
				String skipAmountString = event.getSkipAmount();
				getPlayer(event).ifPresent(player -> {
					if(skipAmountString == null)
					{
						//Skip current song
						player.skipTrack();
					}
					else if(!isInteger(skipAmountString))
					{
						event.getTextChannel().createMessage("That's not a valid number!")
							.subscribe();
					}
					else
					{
						int skipAmount = Integer.parseInt(skipAmountString);
						player.skipTrack(skipAmount);
					}
				});
			});

	}

	private void handleCurrentTrackRequestEvent()
	{
		dispatcher.on(CurrentTrackRequestEvent.class)
			.flatMap(event -> {
				Optional<MusicPlayer> playerOpts = getPlayer(event);
				if (!playerOpts.isPresent()){
					return Mono.empty();
				}

				AudioTrack currentSong = playerOpts.get().getCurrentTrack();

				return event.getTextChannel().createEmbed(spec -> {
					spec.setTitle("Current song: ");
					spec.setDescription("**" + (currentSong != null
						? TrackUtils.getReadableTrack(currentSong)
						: "none") + "**");
				});
			})
			.subscribe();
	}

	private void handleLoopPlaylistEvent()
	{
		dispatcher.on(LoopPlaylistEvent.class)
			.flatMap(event -> {
				TextChannel channel = event.getTextChannel();

				String loopEnabled = event.getLoopEnabled() == null
					? ""
					: event.getLoopEnabled();

				return getPlayer(event).map(player -> {
					switch(loopEnabled)
					{
						case "on":
							player.setLoopEnabled(true);
							return channel.createMessage("Looping enabled.");
						case "off":
							player.setLoopEnabled(false);
							return channel.createMessage("Looping disabled.");
						default:
							return channel.createMessage("Invalid loop command. Specify on or off, e.g. \"!loop on\"");
					}
				}).orElse(Mono.empty());
			})
			.subscribe();

	}

	private void handleShuffleEvent()
	{
		dispatcher.on(ShufflePlaylistEvent.class)
			.flatMap(event -> getPlayer(event)
				.map(player -> {
					player.shuffle();

					return event.getTextChannel().createMessage("Playlist shuffled!");
				})
				.orElse(Mono.empty())
			).subscribe();

	}

	private void handleShowPlaylistEvent()
	{
		dispatcher.on(ShowPlaylistEvent.class)
			.flatMap(event -> {
				Optional<MusicPlayer> playerOpts = getPlayer(event);
				if (!playerOpts.isPresent()){
					return Mono.empty();
				}

				List<AudioTrack> playlist = playerOpts.get().getPlayList();
				TextChannel channel = event.getTextChannel();
				if(playlist.isEmpty())
				{
					return channel.createMessage("No songs added to the playlist.");
				}
				else
				{
					StringBuilder sb = new StringBuilder("```");

					int i = 1;
					String moreSongs = " \n and %d more...";
					for(AudioTrack track : playlist)
					{

						// The playlist size may be too large to send a message in
						// as the maximum message may be IMessage.MAX_MESSAGE_LENGTH characters long.
						// Reserving two digits for the amount of songs
						String trackString = String.format("\n %d. %s", i++, TrackUtils.getReadableTrack(track));
						if((trackString.length() + sb.length()) <
							(Message.MAX_CONTENT_LENGTH - moreSongs.length() - 2))
						{
							sb.append(trackString);
						}
						else
						{
							// We have reached the limit, print out the more songs string
							sb.append(String.format(moreSongs, playlist.size() - i + 1));
							break;
						}
					}
					sb.append("```");

					return channel.createMessage(sb.toString())
						.flatMap(msg -> msg.addReaction(ReactionEmoji.unicode(PLAY_PAUSE_EMOJI))
							.then(msg.addReaction(ReactionEmoji.unicode(NEXT_TRACK_EMOJI))));
				}
			}).subscribe();
	}

	private void handleClearPlaylistEvent()
	{
		dispatcher.on(ClearPlaylistEvent.class)
			.flatMap(event -> getPlayer(event)
				.map(player -> {
					player.clearPlaylist();
					return event.getTextChannel().createMessage("Playlist cleared.");
				}).orElse(Mono.empty())
			)
			.subscribe();
	}

	private void handlePauseEvent()
	{
		dispatcher.on(PauseEvent.class)
			.flatMap(event -> getPlayer(event)
				.map(player -> {
					String shouldPause = event.shouldPause() == null
						? ""
						: event.shouldPause();

					switch(shouldPause)
					{
						case "on":
							player.pause(true);
							break;
						case "off":
							player.pause(false);
							break;
						default:
							return event.getTextChannel()
								.createMessage("Invalid pause command. Specify on or off, e.g. \"!pause on\"");
					}

					return Mono.empty();
				}).orElse(Mono.empty())
			).subscribe();
	}

	private void handleReactionEvent()
	{
		Flux.merge(
			dispatcher.on(ReactionAddEvent.class)
				.flatMap(event -> Mono.zip(event.getUser(), Mono.just(event.getEmoji()), Mono.just(event.getGuildId()))),
			dispatcher.on(ReactionRemoveEvent.class)
				.flatMap(event -> Mono.zip(event.getUser(), Mono.just(event.getEmoji()), Mono.just(event.getGuildId()))))
			.filter(obj -> !obj.getT1().isBot())
			.subscribe(tuple -> {
				Optional<ReactionEmoji.Unicode> unicode = tuple.getT2().asUnicodeEmoji();
				Optional<Snowflake> guildId = tuple.getT3();

				if (guildId.isPresent() && unicode.isPresent()){
					Optional<MusicPlayer> playerOpts = playerFactory
						.getMusicPlayer(guildId.get());

					playerOpts.ifPresent(player -> {
						switch(unicode.get().getRaw())
						{
							case PLAY_PAUSE_EMOJI:
								player.pause(!player.isPaused());
								break;
							case NEXT_TRACK_EMOJI:
								player.skipTrack();
								break;
						}
					});
				}
			});
	}
	
	private boolean isInteger(String s)
	{
		return isInteger(s, 10);
	}

	private boolean isInteger(String s, int radix)
	{
		if(s.isEmpty())
		{
			return false;
		}
		for(int i = 0; i < s.length(); i++)
		{
			if(i == 0 && s.charAt(i) == '-')
			{
				if(s.length() == 1)
				{
					return false;
				}
				else
				{
					continue;
				}
			}
			if(Character.digit(s.charAt(i), radix) < 0)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the {@link MusicPlayer} associated with the specified {@link KensaEvent}
	 *
	 * @param event
	 * 		the {@link KensaEvent}
	 *
	 * @return
	 * 		the {@link MusicPlayer} associated with the specified {@link KensaEvent}
	 */
	private Optional<MusicPlayer> getPlayer(KensaEvent event)
	{
		return playerFactory.getMusicPlayer(event);
	}
}