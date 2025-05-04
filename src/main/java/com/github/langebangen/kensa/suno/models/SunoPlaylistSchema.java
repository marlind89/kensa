package com.github.langebangen.kensa.suno.models;

import java.util.List;

public record SunoPlaylistSchema(
    String entity_type,
    String id,
    List<SunoPlaylistClip> playlist_clips,
    String name,
    String descriptionm,
    String user_display_name
) {
}