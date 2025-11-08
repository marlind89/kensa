package com.github.langebangen.kensa.audio.lavaplayer;

import com.github.langebangen.kensa.audio.MusicPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public record AudioMusicPlayer(
    AudioPlayer audioPlayer,
    MusicPlayer musicPlayer
)
{
}
