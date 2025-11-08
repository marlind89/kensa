package com.github.langebangen.kensa.suno.models;

public record SunoPlaylistClip(
    SunoClip clip,
    double relative_index
)
{
}